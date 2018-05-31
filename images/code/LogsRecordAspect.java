package com.chinaredstar.uc.web.mdms.config.aop;
import com.alibaba.fastjson.JSON;
import com.chinaredstar.common.util.util.JacksonUtils;
import com.chinaredstar.common.util.util.PropertyUtils;
import com.chinaredstar.common.util.util.WebUtils;
import com.chinaredstar.uc.dubbo.core.api.IEmployeeService;
import com.chinaredstar.uc.dubbo.core.api.exception.RedstarException;
import com.chinaredstar.uc.jdbc.uc.dao.*;
import com.chinaredstar.uc.jdbc.uc.po.LogsRecord;
import com.chinaredstar.uc.web.mdms.utils.Constant;
import com.chinaredstar.uc.web.mdms.utils.PageJson;
import com.chinaredstar.uc.web.mdms.utils.ServiceResult;
import com.chinaredstar.urms.common.vo.UserDetail;
import com.chinaredstar.urms.core.config.UrmsSystemConfig;
import com.chinaredstar.urms.security.utils.UrmsSessionUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.session.Session;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * @author lenovo aop拦截系统操作日志记录
 */
@Aspect
@Configuration
public class LogsRecordAspect {
	private static final Logger logger = LoggerFactory.getLogger(LogsRecordAspect.class);
	@Resource
	private ClientPermissionsMapper clientPermissionsMapper;
	@Resource
	private LogsRecordMapper logsRecordMapper;
	@Resource
	private LogsRecordObjectTypeMapper logsRecordObjectTypeMapper;
	@Resource
	private ClientRoleEmployeesMapper clientRoleEmployeesMapper;
	@Resource
	private ClientRolesMapper clientRolesMapper;
	@Resource
	private IEmployeeService employeeService;
	@Autowired
	private AmqpTemplate amqpTemplate;
	String url = null, reqIpAddress = null, beforeContent = null, afterContent = null;
	String operateType = "04";// 01=add;02=del;03=update;04=del
	JSONObject jsonObject = null;// content-json
	Map<Object, Object> logsRecordMap = null;
	@Autowired
	private UrmsSystemConfig urmsSystemConfig;
	@Pointcut("@annotation(com.chinaredstar.uc.web.mdms.config.aop.LogsRecordSearchService)")
	public void logsPointcutSearch() {
	}
	@Pointcut("@annotation(com.chinaredstar.uc.web.mdms.config.aop.LogsRecordInsertService)")
	public void logsPointcutInsert() {
	}
	@Pointcut("@annotation(com.chinaredstar.uc.web.mdms.config.aop.LogsRecordModifyService)")
	public void logsPointcutModify() {
	}
	/**
	 * 搜索业务逻辑方法切入点;find\select\query\get
	 */
	@Pointcut("execution(* com.chinaredstar.uc.web.mdms.service.*.*(..))")
	public void allPointcut() {
	}
	@Around(value = "logsPointcutInsert()")
	public Object insertProcess(ProceedingJoinPoint point) throws Throwable {
		Object returnValue = null;
		Object[] args = point.getArgs();
		operateType = LogsConstant.OPERATE_TYPE_ADD;
		try {
			returnValue = point.proceed(args);
			this.saveLogsRecord(point, null, null, null,  null);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("logsPointcutInsert方法异常信息:{}", e.getMessage());
		}
		return returnValue!=null?returnValue:getDefaultReturnValue(point,null);
	}
	@Around(value = "logsPointcutModify()")
	public Object modifyProcess(ProceedingJoinPoint point) throws Throwable {
		Map<Object, Object> logsRecordAfterMap = new HashMap<>();// after modify
		operateType = LogsConstant.OPERATE_TYPE_MODIFY;
		Object[] object = point.getArgs();
		Object afterObj = null;
		String failMessage = null;
		try {
			// after update
			// return data
			afterObj = point.proceed(object);
			if (afterObj != null) {
				Class cls = afterObj.getClass();
				BeanInfo beanInfo = Introspector.getBeanInfo(cls);
				PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
				for (int i = 0; i < propertyDescriptors.length; i++) {
					PropertyDescriptor pd = propertyDescriptors[i];
					String aa = pd.getName();
					if (aa.equals("class")) {
						continue;
					}
					// PropertyDescriptor pd = new
					// PropertyDescriptor(propertyDescriptors[i].getName(),
					// cls);
					Method getMethod = pd.getReadMethod();
					Object obj = getMethod.invoke(afterObj);
					logsRecordAfterMap.put(pd.getName(), obj);
				}
				afterContent = JacksonUtils.toJSon(logsRecordAfterMap);
			}
			// return type
			// Class returnType = ((MethodSignature)
			// point.getSignature()).getReturnType();
			this.saveLogsRecord(point, null, null, null, afterContent);
		} catch (Exception e) {
			if(e instanceof RedstarException){
				failMessage = e.getMessage();
			}
		}
		return afterObj!=null?afterObj:getDefaultReturnValue(point,failMessage);
	}
	@Before(value = "allPointcut()")
	public void beforeMethod(JoinPoint joinPoint) {
		// session = SecurityUtils.getSubject().getSession();
		org.apache.shiro.session.Session session = UrmsSessionUtils.getSession();
		if (session == null) {
			return;
		}
	}
	// aop search
	@AfterReturning(value = "logsPointcutSearch()", argNames = "returnValue", returning = "returnValue")
	public void logsRecordAspect(JoinPoint point, Object returnValue) {
		try {
			operateType = LogsConstant.OPERATE_TYPE_SECHER;
			this.saveLogsRecord(point, null, null, null,  null);
		} catch (Exception e) {
			return;
		}
	}
	@AfterThrowing(pointcut = "allPointcut()", throwing = "e")
	public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
		String params = "";
		if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
			for (int i = 0; i < joinPoint.getArgs().length; i++) {
				params += JSON.toJSONString(joinPoint.getArgs()[i]) + ";";
			}
		}
		try {
			/* ========控制台输出========= */
			System.out.println("=====异常通知开始=====");
			System.out.println("异常代码:" + e.getClass().getName());
			System.out.println("异常信息:" + e.getMessage());
			System.out.println("异常方法:"
					+ (joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "()"));
			System.out.println("方法描述:" + getServiceMthodDescription(joinPoint));
			System.out.println("请求IP:" + reqIpAddress);
			System.out.println("请求参数:" + params);
			// 记录本地异常日志
			System.out.println("=====异常通知结束=====");
		} catch (Exception ex) {
			logger.error("==异常通知异常==");
			logger.error("异常信息:{}", ex.getMessage());
		}
	}
	private boolean isBaseDataType(Object attributeValue) {
		return attributeValue instanceof String || attributeValue instanceof Integer || attributeValue instanceof Byte
				|| attributeValue instanceof Long || attributeValue instanceof Double || attributeValue instanceof Float
				|| attributeValue instanceof Character || attributeValue instanceof Short
				|| attributeValue instanceof BigDecimal || attributeValue instanceof BigInteger
				|| attributeValue instanceof Boolean || attributeValue instanceof Date
				|| attributeValue instanceof DateTime;
	}
	/**
	 * 获取注解中对方法的描述信息 用于service层注解
	 *
	 * @param joinPoint
	 *            切点
	 * @return 方法描述
	 * @throws Exception
	 */
	private String[] getServiceMthodDescription(JoinPoint joinPoint) throws Exception {
		String targetName = joinPoint.getTarget().getClass().getName();
		String methodName = joinPoint.getSignature().getName();
		Object[] arguments = joinPoint.getArgs();
		Class targetClass = Class.forName(targetName);
		Method[] methods = targetClass.getMethods();
		String[] str = new String[3];
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				Class[] clazzs = method.getParameterTypes();
				if (clazzs.length == arguments.length) {
					switch (operateType) {
						case "01":
							str[0] = method.getAnnotation(LogsRecordInsertService.class).parentCode().getKey();
							str[1] = method.getAnnotation(LogsRecordInsertService.class).funcCode().getKey();
							break;
						case "03":
							str[0] = method.getAnnotation(LogsRecordModifyService.class).parentCode().getKey();
							str[1] = method.getAnnotation(LogsRecordModifyService.class).funcCode().getKey();
							break;
						default:// del\search
							str[0] = method.getAnnotation(LogsRecordSearchService.class).parentCode().getKey();
							str[1] = method.getAnnotation(LogsRecordSearchService.class).funcCode().getKey();
							str[2] = method.getAnnotation(LogsRecordSearchService.class).reservedField();
					}
				}
			}
		}
		return str;
	}
	/**
	 * @param operateContent
	 *            内容
	 * @param funcName
	 *            功能
	 * @param operateUrl
	 *            请求url
	 * @param operateMethod
	 *            操作方法
	 * @param operateType
	 *            操作类型
	 * @param objectTypeName
	 *            配置表中已配置的对象类型的code
	 * @param objectColName
	 *            关联反查的字段
	 * @param objectTableName
	 *            关联反查的表 void
	 * @param beforeContent
	 *            更新前值
	 * @param afterContent
	 *            更新后值 lenovo
	 * @throws Exception
	 */
	private void saveLogsRecord(JoinPoint point, String objectColName, String objectColVal, String objectTableName,
								String afterContent) throws Exception {
		logger.info("进入saveLogsRecord方法！");
		logsRecordMap = new HashMap<>();
		String operateVo = null;
		try {
			Object[] object = point.getArgs();
			if (object == null || object.length == 0) {
				return;
			}
			for (int i = 0; i < object.length; i++) {
				Object obj = object[i];
				if (isBaseDataType(obj)) {
					logsRecordMap.put(point.getSignature().getName(), obj);
				} else {
					Class clazz = obj.getClass();
					// uploadFile type
					if (clazz.getName().indexOf("MultipartFile") > -1) {
						continue;
					}
					Field[] fields = obj.getClass().getDeclaredFields();// 获得属性
					for (Field field : fields) {
						PropertyDescriptor pd = null;
						try {
							pd = new PropertyDescriptor(field.getName(), clazz);
						} catch (Exception e) {
							// 不存在标准get属性
							continue;
						}
						// get get method
						Method getMethod = pd.getReadMethod();
						Object objVal = getMethod.invoke(obj);
						if (objVal == null || objVal.equals("")) {
							continue;
						}
						if (field.getName().equals("isLogRecord")) {
							if (!(Boolean) objVal) {
								return;
							}
						} else {
							logsRecordMap.put(field.getName(), objVal.toString());
						}
					}
				}
			}
			if (logsRecordMap.isEmpty()) {
				return;
			}
			LogsRecord logRecord = new LogsRecord();
			String parentCode = this.getServiceMthodDescription(point)[0];
			String parentName = LogsEnums.getValueByKey(this.getServiceMthodDescription(point)[0]);
			String funcCode = this.getServiceMthodDescription(point)[1];
			String funcName = LogsEnums.getValueByKey(this.getServiceMthodDescription(point)[1]);
			logRecord.setOperateVo(operateVo);
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getRequest();
			url = request.getRequestURI();
			reqIpAddress = WebUtils.getRemoteAddr(request);
			String clientId = PropertyUtils.getProperty("client.appId");
			if (StringUtils.isNotBlank(clientId)) {
				logRecord.setClientId(clientId);
			} else {
				logRecord.setClientId("-1");
			}
			logRecord.setModuleCode(parentCode);
			logRecord.setModuleName(parentName);
			logRecord.setAppIp(reqIpAddress);
			logRecord.setReqUrl(url);
			logRecord.setFunCode(funcCode);
			logRecord.setFunName(funcName);
			Session session = UrmsSessionUtils.getSession();
			logRecord.setUserId(session
					.getAttribute(com.chinaredstar.uc.session.Constants.DEFAULT_PRINCIPAL_SESSION_KEY).toString());
			String urmsRoleStr1 = session.getAttribute("E_USER_ROLE").toString();
			String roleCode = "", roleName = "";
			if (StringUtils.isNotBlank(session.getAttribute("E_USER_ROLE").toString())) {
				String urmsRoleStr2 = session.getAttribute("E_USER_ROLE").toString();
				if (urmsRoleStr2.indexOf(",") > -1) {
					String[] strings = urmsRoleStr2.split(",");
					for (int i = 0; i < strings.length; i++) {
						if (strings[i].substring(0, 4).equals(urmsSystemConfig.getCode())) {
							roleCode = strings[i].split(":")[1];
							roleName = strings[i].split(":")[2];
							break;
						}
					}
				} else {
					roleCode = urmsRoleStr2.split(":")[1];
					roleName = urmsRoleStr2.split(":")[2];
				}
			}
			UserDetail userDetail = UrmsSessionUtils.getUserDetail();
			logger.info("获取urms用户session1:{},session2:{}", UrmsSessionUtils.getSession().getId(), session.getId());
			logger.info("获取urms会话，用户ID:{};获取用户角色信息(session):{};获取用户角色信息(变量1):{};用户角色code2:|{}|;用户角色描述:{}",
					session.getAttribute(com.chinaredstar.uc.session.Constants.DEFAULT_PRINCIPAL_SESSION_KEY)
							.toString(),
					userDetail.getName(), session.getAttribute("E_USER_ROLE").toString(), urmsRoleStr1, roleCode,
					roleName);
			if (StringUtils.isNotBlank(roleCode)) {
				logRecord.setOperateRoleCode(roleCode);
			} else {
				logRecord.setOperateRoleCode("-1");
			}
			if (StringUtils.isNotBlank(roleName)) {
				logRecord.setOperateRoleDesc(roleName);
			} else {
				logRecord.setOperateRoleDesc("-1");
			}
			logRecord.setUserLabel(session.getId().toString());
			if (!operateType.equals(LogsConstant.OPERATE_TYPE_MODIFY)) {
				logRecord.setOperateContent(JSON.toJSONString(logsRecordMap));
			} else {
//				logRecord.setOperateContent("");
				beforeContent = JSON.toJSONString(logsRecordMap);// 修改
				if (StringUtils.isNotBlank(beforeContent)){
					logRecord.setOperateContent(beforeContent);
				}
			}
			logRecord.setUserName(userDetail.getName());
			String marketCode = userDetail.getOmsMarketId();
			if (StringUtils.isNotBlank(marketCode)) {
				// is market
				logRecord.setAttributeRole(true);
			} else {
				// is company group
				logRecord.setAttributeRole(false);
			}
			logRecord.setMarketCode(marketCode);
			logRecord.setMarketDesc(userDetail.getMarketDesc());
			logRecord.setOperateType(operateType);
			logRecord.setOperateUrl(point.getTarget().getClass().getName());
			logRecord.setOperateMethod(point.getSignature().getName());
			logRecord.setCreatedDt(new Date());
			if (StringUtils.isNotBlank(objectColName)) {
				logRecord.setObjectColName(objectColName);
			}
			if (StringUtils.isNotBlank(objectColVal)) {
				logRecord.setObjectColVal(objectColVal);
			}
			if (StringUtils.isNotBlank(objectTableName)) {
				logRecord.setObjectTableName(objectTableName);
			}
			if (StringUtils.isNotBlank(beforeContent)) {
				logRecord.setModifyBeforeContent(beforeContent);
			}
			if (StringUtils.isNotBlank(afterContent)) {
				logRecord.setModifyAfterContent(afterContent);
			}
			logger.info("saveLogsRecord方法,ModuleCode: "+ JacksonUtils.toJSon(logRecord));
			amqpTemplate.convertAndSend("uc_common_logs", JacksonUtils.toJSon(logRecord));
		} catch (Exception e) {
			logger.info("抛出异常：saveLogsRecord方法:  " ,e);
			e.printStackTrace();
			return;
		}
	}
	/**
	 * @param point
	 * @return
	 * @Transanction 和 aop 共存时的默认异常处理
	 */
	private Object getDefaultReturnValue(ProceedingJoinPoint point,String message) {
		Object returnValue = null;
		//设置抛异常后的失败默认值
		String failedMessage = StringUtils.isEmpty(message)?"请求失败，请重试":message;
		Signature signature = point.getSignature();
		Class returnType = ((MethodSignature) signature).getReturnType();
		if (returnType.equals(PageJson.class)) {
			returnValue = new PageJson(Constant.FAILED, failedMessage);
		} else if (returnType.equals(ServiceResult.class)) {
			returnValue = new ServiceResult(Constant.INT_FAILED, failedMessage);
		} else if (returnType.equals(com.chinaredstar.uc.dubbo.core.api.vo.ServiceResult.class)) {
			returnValue = new com.chinaredstar.uc.dubbo.core.api.vo.ServiceResult(Constant.INT_FAILED, failedMessage);
		}
		return returnValue;
	}
	public static void main(String[] args) {
		String aString = "mdms:rolecode1:角色1,mdms:rolecode2:角色2";
		String roleCode = "", roleName = "";
		String systemId = "mdms";
		if (aString.indexOf(",") > -1) {
			String[] strings = aString.split(",");
			if (strings[0].substring(0, 4).equals(systemId)) {
				roleCode = strings[0].split(":")[1];
				roleName = strings[0].split(":")[2];
			}
			System.out.println(roleCode);
		}
	}
}