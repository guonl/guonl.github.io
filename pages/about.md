---
layout: page
title: About
description: 总要留一下一些脚印，以方便找到来时的路……
keywords: Lanny
comments: true
menu: 关于
permalink: /about/
---

总要留一下一些脚印，以方便找到来时的路……

## 联系

{% for website in site.data.social %}
* {{ website.sitename }}：[@{{ website.name }}]({{ website.url }})
{% endfor %}

## Skill Keywords

{% for category in site.data.skills %}
### {{ category.name }}
<div class="btn-inline">
{% for keyword in category.keywords %}
<button class="btn btn-outline" type="button">{{ keyword }}</button>
{% endfor %}
</div>
{% endfor %}
