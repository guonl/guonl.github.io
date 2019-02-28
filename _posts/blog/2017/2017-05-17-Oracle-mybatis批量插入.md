---
layout: post
title: Oracle MyBatis批量插入
categories: Oracle
description: Oracle MyBatis批量插入
keywords: Oracle,MyBatis
---

    <insert id="insertCards" parameterType="java.util.List">
        BEGIN
        <foreach collection="list" item="item" index="index" separator=";">
            INSERT INTO fpc_card_bank (id, order_no, check_no, auth_no, cad_type_id, zhx_card_no, storeid, amount,
            if_bill, buy_date, create_date) VALUES
            (TABLES_SEQ.nextval, #{item.buy_order_no}, #{item.check_no}, #{item.auth_no}, #{item.card_type}, #{item.zhx_card_no},
            #{item.storied}, #{item.amount},
            '0', #{item.swap_date}, sysdate)
        </foreach>
        ;END ;
	</insert>