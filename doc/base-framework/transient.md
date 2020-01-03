# transient

&emsp;&emsp;一般情况下,`transient`修饰的变量在序列化之后将无法被访问，不再是持久化的一部份，但是如果继承了`Externalizable`，`transient`修饰符将不会起作用
