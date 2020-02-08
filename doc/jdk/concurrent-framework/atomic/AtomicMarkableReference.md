# AtomicMarkableReference

## 和`AtomicStampedReference`的区别

&emsp;&emsp;和`AtomicStampedReference`的区别主要就是`xxStampedxx`中的`stamped`可以设定值，而`xxMarkablexx`只能设定`true`或者`false`

## 相同点

&emsp;&emsp;解决了`ABA`问题
