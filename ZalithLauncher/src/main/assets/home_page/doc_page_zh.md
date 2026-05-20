# 自定义主页使用指南！

欢迎使用 Zalith Launcher2 的自定义主页！你可以使用 **Markdown** 语法来编写主页！  
除了标准 Markdown 外，你还可以使用以下扩展组件来丰富你的主页

### 扩展规则
- **注释**:
    - 以 `//` 开头的行都会被忽略，你可以使用注释来对某些内容进行解释说明
    - 但 Markdown 代码块中的注释行不会被忽略
- **组件**:
    - 扩展组件以 `...` 开头，例如 `...button`
    - 组件支持使用属性，来控制外观或者行为，在组件行空一格开始，可填写对应的属性

//这是一行注释，你只能在编辑文件的时候看见我！

---

### 随机文本块
在一个区块内填入多段文本，主页在加载时，将随机显示其中的一段文本

**语法**  
...random-start
这是一段文本，权重默认为 1.0
weight(2.0): 这是第二段文本，权重被配置为 2.0
这是第三段文本
...random-end

**参数说明：**
- 与其他组件不同，该组件的参数为内嵌参数，直接嵌入到实际值行首
- `weight`: 
  - 可填写该段文本的具体的权重值，支持整数、小数，可选
  - 主页将按权重随机抽取文本

> 该组件仅支持在基础的 Markdown 内使用
> 该组件不会生效于标准 Markdown 容器或其他扩展组件内

---

### 卡片组件
用于将内容包裹在一个有背景和圆角的容器中

**语法：**
...card-start title="我的卡片" shape=large contentPadding=(16, 12)
这里是卡片内部的内容，支持标准 **Markdown**
...card-end

**参数说明：**
- `title`: 卡片标题，可选，不存在或不填写时，卡片会不显示标题块
...card-start title="示例"
这个卡片展示了如何**配置标题**
...card-end
- `shape`: 圆角大小，可选
    - 支持使用预设大小，由 MaterialTheme 提供：`extraSmall`, `small`, `medium`, `large`, `extraLarge`
    - 可以使用具体的数值来控制圆角大小：`12dp`，支持整数小数
    - 可以使用百分比圆角大小，如：`20%`，仅支持整数百分比
    - 由于该属性的数值是区分单位的，所以必须带上单位，否则该属性不生效
...card-start title="形状示例" shape=medium
中等圆角的卡片！
...card-end

...card-start title="形状示例" shape=4dp
4dp 圆角的卡片！
...card-end

...card-start title="形状示例" shape=20%
20% 圆角的卡片！
...card-end
- `contentPadding`: 控制卡片的内边距
    - 格式：`(all)`, `(horizontal, vertical)`, `(left, top, right, bottom)`，支持整数、小数
    - 由于该属性的数值的单位只能是 `dp`，所以此处无需带上单位，否则该属性不生效
...card-start title="内容内边距示例" contentPadding=(12)
contentPadding=(12)
...card-end

...card-start title="内容内边距示例" contentPadding=(12, 8)
contentPadding=(12, 8)
...card-end

...card-start title="内容内边距示例" contentPadding=(4, 4, 12, 12)
contentPadding=(4, 4, 12, 12)
...card-end

> 卡片组件不支持 `width` 和 `weight` 属性，其宽度始终跟随主页宽度
> 卡片组件为不可组合组件，它不能被装进布局组件（Row/Column）中

---

### 按钮组件
创建一个可以点击的按钮

**语法：**
...button text="访问哔哩哔哩" event="url {https://www.bilibili.com/}"
...button-outlined text="检查更新" event="check_update"

**按钮样式：**
- `...button`: 填充样式
...button text="填充样式"
- `...button-outlined`: 外框样式
...button-outlined text="外框样式"
- `...button-filled-tonal`: 色调填充样式
...button-filled-tonal text="色调填充样式"
- `...button-text`: 纯文字样式
...button-text text="纯文字样式"

**参数说明：**
- `text`: 按钮显示的文字，必填，值需使用双引号包裹
- `event`: 触发的事件，可选，值需使用双引号包裹，使用花括号包裹事件数据
    - `url{...}`: 在浏览器中打开链接
    - `check_update`: 触发启动器检查更新
    - `launch_game{server=...}`: 启动当前选中的版本
      - 参数 `server`：指定快速启动并加入的服务器，可选
    - `copy{...}`: 复制指定内容
    - `refresh_page`: 刷新当前主页
    - `share_game_log`: 分享当前游戏版本的日志
- `width`: 按钮的宽度，可选
    - 可使用百分比宽度，会根据主页的实际宽度、所在布局组件的宽度计算：`50%`，仅支持整数百分比
    - 可使用 DP 单位来设置更具体的宽度：`200dp`，支持整数、小数
    - 由于该属性的数值是区分单位的，所以必须带上单位，否则该属性不生效
    - 示例：
      ...button text="按钮1" width=50%
      ...button text="按钮2" width=120dp
- `shape`: 按钮的形状，可选，与卡片的形状参数一致
- `weight`: 仅在 Row 或 Column 内部可用，见下方布局组件的说明

> 按钮属于内容组件，若未显式指定 `width` 或 `weight`，其宽度由文字长度决定（受容器限制不会超出主页）

---

### 横向布局 (`Row`)
将多个组件或布局组件横向排版  
这个组件对齐的是 Jetpack Compose 中的 Row 组件

**语法：**
...row-start horizontal=spacedBy(8) vertical=Center
    ...button text="按钮1" weight=(1)
    ...button text="按钮2" weight=(1)
...row-end

**参数说明：**
- `horizontal`: 水平排列参数
    - 可使用的排列方式：`Start`, `Center`, `End`, `SpaceEvenly`, `SpaceBetween`, `SpaceAround`
    - 可使用 `spacedBy` 控制子项之间的距离：
        - 仅控制距离：`spacedBy(12)`，支持整数、小数
        - 控制距离的同时，控制水平排列方式：`spacedBy(12, Start)`，此处仅支持使用值：`Start`, `Center`, `End`
        - 该属性的数值的单位只能是 `dp`，所以此处无需带上单位，否则该属性不生效
    - 示例：
...row-start horizontal=Start
...button text="Start"
...button text="Start"
...row-end

...row-start horizontal=spacedBy(12)
...button text="spacedBy(12)"
...button text="spacedBy(12)"
...row-end

...row-start horizontal=spacedBy(12, End)
...button text="spacedBy(12, End)"
...button text="spacedBy(12, End)"
...row-end
- `vertical`: 垂直对齐
    - 可使用的排列方式：`Top`, `Center` (或更符合语义的 `CenterVertically`), `Bottom`
    - 垂直居中示例：
...row-start vertical=Center
...button text="Center"
...image url="https://www.baidu.com/img/flexible/logo/pc/result.png" width=10%
...row-end
- **子项属性 `weight`**:
    - 这是仅在 Row 组件内部可使用的属性，用于分配子组件的宽度
    - 可填写具体的权重值，将会根据主页的实际宽度为子组件分配宽度，支持整数、小数
    - 若添加 `noFill` 配置，则会使组件占有该权重的宽度，但组件并不会真的填满该区域（例如按钮可能仍然保持内容宽度）
    - 该属性的数值没有单位，仅表示比例
      - 示例：
...row-start
  ...button text="按钮1" weight=(1)
  ...button text="按钮2" weight=(1)
...row-end
...row-start
  ...button text="按钮1" weight=(1)
  ...button text="按钮2" weight=(1, noFill)
...row-end
- `width`: 布局组件的宽度，可选，与按钮的属性一致；若该组件是根组件，则默认值为 `100%`

> Row 或 Column 内部可放置按钮、图片、或嵌套的 Row/Column 组件
> 当然也可以放置 Markdown 文本，不过 Markdown 文本不能像其他高级组件一样，进行缩进处理！否则会出现渲染异常

---

### 纵向布局 (`Column`)
将多个组件或布局组件纵向排版  
这个组件对齐的是 Jetpack Compose 中的 Column 组件

**语法：**
...column-start vertical=spacedBy(8) horizontal=Center
    ...button text="按钮1"
    ...button text="按钮2"
...column-end

**参数说明：**
- `vertical`: 垂直排列参数
    - 可使用的排列方式：`Top`, `Center`, `Bottom`, `SpaceEvenly`, `SpaceBetween`, `SpaceAround`
    - 可使用 `spacedBy` 控制子项之间的距离：
        - 仅控制距离：`spacedBy(12)`，支持整数、小数
        - 控制距离的同时，控制垂直排列方式：`spacedBy(12, Top)`，此处仅支持使用值：`Top`, `Center`, `Bottom`
        - 该属性的数值的单位只能是 `dp`，所以此处无需带上单位，否则该属性不生效
    - 示例：
...column-start vertical=spacedBy(8)
  ...button text="按钮1"
  ...button text="按钮2"
...column-end
- `horizontal`: 水平对齐
    - 可使用的对齐方式：`Start`, `Center` (或更符合语义的 `CenterHorizontally`), `End`
    - 水平居中示例：
...column-start horizontal=Center
  ...button text="居中按钮"
  ...image url="https://www.baidu.com/img/flexible/logo/pc/result.png" width=30%
...column-end
- `width`: 布局组件的宽度，可选，与按钮的属性一致；若该组件是根组件，则默认值为 `100%`

> 与 Row 相同，但 Column 不支持对子组件使用 `weight` 属性

---

### 布局嵌套
`Row` 与 `Column` 组件支持互相嵌套，你可以通过嵌套来实现复杂的界面布局

**示例：**
...card-start title="嵌套示例"
    ...column-start vertical=spacedBy(8)
        ...row-start horizontal=spacedBy(8)
            ...button text="按钮1" weight=(1)
            ...button text="按钮2" weight=(1)
        ...row-end
        ...image url="https://picsum.photos/400/100" width=100% shape=8dp
    ...column-end
    ...row-start horizontal=spacedBy(8)
        ...button text="按钮3" weight=(1)
        ...column-start weight=(1)
            ...button text="按钮4" width=100%
            ...button text="按钮5" width=100%
        ...column-end
    ...row-end
...card-end

---

### 图片组件
虽然 Markdown 已经包括了定义图片的语法，但它可操作性太小，例如无法调整位置、宽度等  
我们可以使用高级图片组件来解决这个痛点

**语法：**
...image url="https://picsum.photos/400" width=40% shape=12dp

**参数说明：**
- `url`: 图片链接，必填
- `width`: 图片的宽度，可选，与按钮宽度属性一致，
- 示例：
...image url="https://picsum.photos/500" width=50%
...image url="https://picsum.photos/600" width=120dp
- `shape`: 图片的圆角大小，与卡片的形状参数一致
- `weight`: 权重，仅在 Row 或 Column 内部可用，且配置 `weight` 后，`width` 会被完全忽略

> 图片属于内容组件，若未显式指定 `width` 或 `weight`，其宽度为图片原始宽度（受容器限制不会超出主页）

---

## 注意事项
- **组件嵌套**:
    - `Row` 与 `Column` 支持无限互相嵌套
    - `Card` 禁止嵌套，否则内部卡片会被当作普通文本
    - `Row`/`Column` 内部可以放置按钮、图片或嵌套的布局组件，也可以放置 Markdown 文本，不过 Markdown 文本不能像其他高级组件一样，进行缩进处理！否则会出现渲染异常
- 标签必须成对出现，如 `...card-start` 与 `...card-end` 必须配对
- 扩展组件不能嵌入标准 Markdown 容器：扩展组件相对独立，并没有彻底融入 Markdown 语法，例如无法将 `...image` 写在 Markdown 的代码块或表格内部
- 图片加载依赖网络，请确保图片链接可访问

虽然我们提供了丰富的扩展组件，但整个主页仍然基于 **Markdown**  
如果你还不熟悉标准 Markdown 语法，建议先花几分钟学习一下，真的很好学！  
[Markdown 菜鸟教程](https://www.runoob.com/markdown/md-tutorial.html)
