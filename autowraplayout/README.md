# :star2:&nbsp;AutoWrapLayout

自动换行布局，支持SDK14(Android4.0)及以上。

## 目录

* [示例demo](#示例demo)
* [功能介绍](#功能介绍)
* [集成方式](#集成方式)
* [属性说明](#属性说明)
* [注意事项](#注意事项)
* [版本记录](#版本记录)
* [关于作者](#关于作者)

## 示例demo

|Demo下载(1.5MB)|示例效果|
|:---:|:---:|
|[点此下载](http://fir.im/SLWidget) 或扫描下面二维码<br/>[![demo](../download.png)](http://fir.im/SLWidget  "扫码下载示例程序")|![gif](./demo_autowraplayout.png)&#32;&#32;&#32;![gif](./demo_autowraplayout.gif)|

## 功能介绍

实现ChildView的自动换行，可根据控件大小自动调整ChildView的宽高进行网格排列。

## 集成方式

在module的`build.gradle`中添加如下代码
```
    dependencies {
        implementation 'cn.simonlee.widget:autowraplayout:1.0.6'
    }
```

## 属性说明

* **首行独占**

    |KEY|VALUE|
    |:---:|:---:|
    |属性名|autowrap_stickFirst|
    |类型|boolean|
    |默认值|false|
    |API|`void` setStickFirst(`boolean` stick)|
    |说明|当设置为true时，第一个child（且必须可见）会独占一行。|

* **单元格宽**

    |KEY|VALUE|
    |:---:|:---:|
    |属性名|autowrap_gridCellWidth|
    |类型|dimension|
    |默认值|0dp|
    |API|`void` setGridCellSize(`int` width, `int` height)|
    |说明|API中的单位为px，当宽度不大于0时，以所有child中宽度的最大值为准|

* **单元格高**

    |KEY|VALUE|
    |:---:|:---:|
    |属性名|autowrap_gridCellWidth|
    |类型|dimension|
    |默认值|0dp|
    |API|`void` setGridCellSize(`int` width, `int` height)|
    |说明|API中的单位为px，当高度不大于0时，以所有child中高度的最大值为准|

* **单元格对齐方式**

    |KEY|VALUE|
    |:---:|:---:|
    |属性名|autowrap_gridCellGravity|
    |类型|flag(`left` &#124; `top` &#124; `right` &#124; `bottom` &#124; `center` &#124; `fill`)|
    |默认值|fill|
    |API|`void` setGridCellGravity(`int` gravity)|
    |说明|布局的宽度被均分后，单元格的尺寸是大于等于child尺寸的。<br/>此属性规定的是child在单元格内的对齐方式|

* **网格线宽**

    |KEY|VALUE|
    |:---:|:---:|
    |属性名|autowrap_gridLineWidth|
    |类型|dimension|
    |默认值|1px|
    |API|`void` setGridLineWidth(`int` width)|
    |说明|API中的单位为px|

* **网格线颜色**

    |KEY|VALUE|
    |:---:|:---:|
    |属性名|autowrap_gridLineColor|
    |类型|color|
    |默认值|0(透明)|
    |API|`void` setGridLineColor(`int` color)|
    |说明|无|

## 注意事项

* **Tips.1**

    ChildView宽高的`match_parent`属性均无效，当`wrap_content`处理。
    **特例：** 首行独占的宽允许`match_parent`生效。

## 版本记录

*  **V1.0.6**   `2018/11/06`

    1. 优化onMeasure方法，解决单元格测量尺寸与实际尺寸不相符的问题。
    2. 修复在某些情况下网格线显示异常的问题。

## 关于作者

限于个人能力有限，些许疏忽失误，欢迎指正。如果提Issue回复不及时可以微信联系我。
如果您觉得有用，请不吝点**Star**:blush:

|Author|E-mail|博客|WeChat|
|:---:|:---:|:---:|:---:|
|Simon Lee|jmlixiaomeng@163.com|[简书](https://www.jianshu.com/u/c35bd597dafb) · [掘金](https://juejin.im/user/5a38846b6fb9a04528469a89)|![wechat](../wechat.png)|

