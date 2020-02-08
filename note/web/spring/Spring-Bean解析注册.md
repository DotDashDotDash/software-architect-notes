# Spring Bean解析注册过程

## Spring的Bean解析和加载的过程

* **定位**
  * **org.springframework.context.support.AbstractXmlApplicationContext**
    * **loadBeanDefinitions**(org.springframework.beans.factory.support.DefaultListableBeanFactory) 通过XmlBeanDefinitionReader加载bean定义
    * **loadBeanDefinitions**(org.springframework.beans.factory.xml.XmlBeanDefinitionReader) 使用传入的XmlBeanDefinitionReader加载bean定义。
  * **org.springframework.beans.factory.support.AbstractBeanDefinitionReader**
    * **loadBeanDefinitions**(org.springframework.core.io.Resource…) 从传入的资源中加载bean定义
  * **org.springframework.beans.factory.xml.XmlBeanDefinitionReader**
    * **loadBeanDefinitions**(org.springframework.core.io.Resource) 从传入的资源中加载bean定义 (xml)
    * **loadBeanDefinitions**(org.springframework.core.io.support.EncodedResource) 从传入的资源中加载bean定义 （xml)
    * **doLoadBeanDefinitions** 开始执行 从指定的XML文件加载bean定义。
    * **registerBeanDefinitions** 注册DOM文档中包含的bean定义。
* **加载**
  * **org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader**
    * **registerBeanDefinitions** 根据“spring-beans”XSD（或历史上的DTD）解析bean定义。
    * **parseBeanDefinitions** 解析文档中根级别的元素：“import”，“alias”，“bean”。
    * **parseDefaultElement** 根据传入的标签进行解析
    * **processBeanDefinition** 处理传入的bean元素，解析bean定义并将其注册到注册表。
    * **registerBeanDefinition** 注册最终的BeanDefinition
* **注册**
  * **org.springframework.beans.factory.support.DefaultListableBeanFactory**
    * **registerBeanDefinition** 完成注册

## Spring Bean的加载注册时序图

<div align=center><img src="/assets/spring1.png"></div>

## 参考链接

* [Spring解析注册过程](https://blog.csdn.net/lei32323/article/details/88770048)
