# Vue3 Arco SuperTable 组件文档

基于 Arco Design Vue 封装的高级表格组件，集成了配置化列、搜索、分页、CRUD 表单、本地配置持久化等功能，旨在通过 JSON 配置快速构建标准化的后台管理表格。

## 1. 组件功能概述

`SuperTable` (Table.vue) 是一个功能强大的数据展示和管理组件。

### 核心功能
*   **配置驱动**：通过 `config` 对象完全控制表格行为，无需写大量模板代码。
*   **智能分页**：支持**前端分页**（一次性加载）和**后端分页**（API 驱动）两种模式一键切换。
*   **自动搜索**：根据配置自动生成搜索栏，支持多种输入类型（文本、下拉、日期、范围等）。
*   **CRUD 集成**：内置新增、编辑、查看、删除逻辑，自动生成表单弹窗。
*   **列配置增强**：支持列显隐、排序、列宽调整、固定列（左侧、右侧）、文本超出隐藏，并支持**本地持久化存储**（LocalStorage）。
*   **高度可定制**：提供丰富的插槽（单元格、表单项、搜索栏、工具栏）。
*   **复杂表单支持**：支持表单验证、联动、多列布局、子表格（One-to-Many）等高级特性。

### 依赖与技术栈
*   Vue 3.x (Composition API)
*   Arco Design Vue 2.x

### 快速开始
1. 安装依赖
   ```bash
   npm install arco-vue3-supertable
   ```
2. 引入组件
   ``` javascript
   <script setup>
    import { reactive, ref } from 'vue';
    // 全局注册之后就不用每个组件引入了
    import {SuperTable} from 'arco-vue3-supertable';
    // 全局引入css之后就不用每个组件引入了
    import "arco-vue3-supertable/dist/arco-vue3-supertable.css";

    const tableData = ref([]);
    const loading = ref(false);
    const selectedKeys = ref([]);
    const config = reactive({
      uniqueId: 'user-table-v1',
      enableLocalStorage: true,
      paginationType: 'frontend',
      rowKey: 'id',
      columns: [
        { title: 'ID', dataIndex: 'id', width: 80 },
        { 
          title: '姓名', 
          dataIndex: 'name', 
          width: 150,
          form: { type: 'input', required: true, creatable: true, editable: true } 
        },
        {
          title: '角色',
          dataIndex: 'role',
          form: { 
            type: 'select', 
            options: [{label: '管理员', value: 'admin'}, {label: '用户', value: 'user'}] 
          }
        }
      ],
      searchFields: [
        { title: '姓名', dataIndex: 'name', type: 'input' }
      ],
      actions: [
        { key: 'edit', label: '编辑' }, // 自动关联 form
        { key: 'delete', label: '删除', type: 'confirm', status: 'danger' }
      ],
      // 表单提交处理
      handleFormSubmit: async ({ mode, data }) => {
        console.log(mode, data);
        // 模拟 API 调用
        if (mode === 'create') tableData.value.push({ id: Date.now(), ...data });
        else { /* 更新逻辑 */ }
      },
      // 自定义按钮处理
      executeAction: async (action, records) => {
        if (action.key === 'delete') {
          // 模拟删除
          tableData.value = tableData.value.filter(item => item.id !== records[0].id);
        }
      }
    });
    </script>
    <template>
      <div class="app-container">
        <SuperTable 
          :config="config" 
          v-model:data="tableData" 
          v-model:loading="loading"
          v-model:selectedKeys="selectedKeys"
        >
          <template #table-top>
            <a-alert>这是一个表格上方插槽示例</a-alert>
          </template>
          <template #table-bottom>
            <div style="padding: 10px; background: #f0f0f0;">
              这是一个表格下方插槽示例
            </div>
          </template>
        </SuperTable>
      </div>
    </template>
   ```
3. 启动全局配置
   ```javascript
   import { createApp } from 'vue'
   import './style.css'
   import App from './App.vue'
   import ArcoVue from '@arco-design/web-vue';
   import Supertable from 'arco-vue3-supertable';
   import ArcoVueIcon from '@arco-design/web-vue/es/icon';
   import '@arco-design/web-vue/dist/arco.css';
   // 引入css之后就不用每个组件引入了
   import "arco-vue3-supertable/dist/arco-vue3-supertable.css";
   const app = createApp(App);
   // 安装之后不用每个组件就引入了
   Supertable.install(app)
   app.use(ArcoVue);
   // 不要忘记icon 否则不会显示按钮图标
   app.use(ArcoVueIcon);
   app.mount('#app');

   ```
---

## 2. config 配置详解

`config` 是组件的核心配置对象，以下是完整参数说明：

### 2.1 基础外观与交互 (UI/UX)

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `cnDesc` | `String` | `'超级表格'` | 表格左上角的标题描述 |
| `tableSize` | `String` | `'small'` | 表格密度: `'mini' \| 'small' \| 'medium' \| 'large'` |
| `rowKey` | `String` | `'key'` | 行数据的唯一主键字段名（如 `id`） |
| `bordered` | `Boolean \| Object` | `{ cell: true }` | 边框配置，`true` 为外框，`{ cell: true }` 为全边框 |
| `stripe` | `Boolean` | `false` | 是否开启斑马纹 |
| `hoverable` | `Boolean` | `true` | 是否开启鼠标悬停效果 |
| `columnResizable` | `Boolean` | `true` | 是否允许拖拽调整列宽 |
| `showHeader` | `Boolean` | `true` | 是否显示表头 |
| `selection` | `Boolean` | `true` | 是否显示多选列（复选框） |
| `showColumnConfig` | `Boolean` | `true` | 是否显示右上角的列设置按钮 |
| `scroll` | `Object` | `{ x: 1200, y: 'auto' }` | 滚动区域配置，如 `{ x: '100%', y: 400 }` |
| `contextMenuEnabled` | `Boolean` | `true` | 是否启用表格行右键菜单功能 |
| `showSearchBar` | `Boolean` | `false` | 是否直接显示搜索条件,true代表直接显示搜索条件（移除掉搜索按钮和关闭按钮）,false代表点击搜索按钮才显示 |
| `tableDisabled` | `Boolean` | `false` | 是否禁用表格操作（禁用所有操作按钮） |
### 2.2 表单与弹窗配置 (Form & Modal)

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `showForm` | `Boolean` | `true` | 是否启用内置的新增/编辑表单弹窗 |
| `modalWidth` | `Number` | `1000` | 表单弹窗的宽度 (px) |
| `formLayout` | `String` | `'horizontal'` | 表单布局: `'horizontal' \| 'vertical' \| 'inline'` |
| `formColumns` | `Number` | `4` | 表单每行显示的列数 |
| `formColGap` | `Number` | `10` | 表单列间距 (px) |
| `formRowGap` | `Number` | `10` | 表单行间距 (px) |

### 2.3 分页与数据 API (Pagination & API)

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `paginationType` | `String` | `'frontend'` | 分页模式: `'frontend'` (前端) \| `'backend'` (后端) |
| `pageSize` | `Number` | `10` | 每页显示条数 |
| `pageSizeOptions` | `Array` | `[5, 10, 20, 50]` | 分页条数下拉选项 |
| `pageApiUrl` | `String` | `''` | 后端分页列表数据的 API 地址 |
| `formAddApiUrl` | `String` | `''` | 新增数据的 API 地址 |
| `formUpdateApiUrl` | `String` | `''` | 更新数据的 API 地址 |
| `formDeleteApiUrl` | `String` | `''` | 删除数据的 API 地址 |
| `tablePaginationAttrs`| `Object` | `{'hide-on-single-page': true}` | 透传给 Arco Pagination 组件的属性 |

### 2.4 样式定制 (Custom Style)

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `hoverColor` | `String` | `'#eef5f8'` | 行悬停背景颜色 |
| `hoverFontColor` | `String` | `''` | 行悬停字体颜色 |
| `headerBgColor` | `String` | `'#eef5f8'` | 表头背景颜色 |
| `headerFontColor` | `String` | `''` | 表头字体颜色 |
| `tableAttrs` | `Object` | `{}` | 透传给 Arco Table 组件的其他属性 |

### 2.5 本地存储配置 (Storage)

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `enableLocalStorage` | `Boolean` | `false` | 是否开启列配置（顺序、宽度、显隐）的本地存储 |
| `uniqueId` | `String` | `''` | **必填**（开启存储时），表格的全局唯一标识 |
| `userCode` | `String` | `''` | 可选，用户标识，用于区分不同用户的配置 |

### 2.6 列配置 (columns)

`columns` 数组中的每个对象代表一列，同时定义了该字段在**表格**和**表单**中的行为。

#### 基础配置

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `title` | `String` | - | 列标题 |
| `dataIndex` | `String` | - | 数据字段名 |
| `width` | `Number` | - | 列宽 |
| `visible` | `Boolean` | `true` | 默认是否显示 |
| `fixed` | `String` | - | 固定列 `'left' \| 'right'` |
| `align` | `String` | `'left'` | 对齐方式 `'left' \| 'right' \| 'center'` |
| `ellipsis` | `Boolean` | - | 超出省略 |
| `sortable` | `Object` | - | 排序配置，如 `{ compare: (a, b) => ... }` |
| `slotName` | `String` | - | 自定义单元格插槽名（可选） |
| `statusMap` | `Object` | - | 状态映射配置，仅当 `slotName` 为 `'status-cell'` 时生效 |
| `form` | `Object` | - | 表单配置对象（见下表） |

#### 表单配置 (columns[].form)

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `type` | `String` | `'input'` | 控件类型：`'input'`, `'select'`, `'number'`, `'date'`, `'radio'`, `'switch'`, `'textarea'`, `'slot'`, `'table'` |
| `slotName` | `String` | - | 自定义表单项插槽名 （当 `type` 为 `'slot'` 时必填）   |
| `creatable` | `Boolean` | `true` | 新增时是否显示 |
| `editable` | `Boolean` | `true` | 编辑时是否显示 |
| `required` | `Boolean` | `false` | 是否必填 |
| `placeholder` | `String` | - | 占位符 |
| `enterNext` | `String` | - | 回车后跳转到的下一个字段名 |
| `oneRow` | `Boolean` | `false` | 表单是否独占一行 |
| `columns` | `Number` | `0` | 表单项所占列数，`0` 代表不生效 |
| `defaultValue` | `*` | - | 默认值 |
| `disabled` | `Boolean \| Function` | - | 是否禁用，支持函数 `(formData, field) => boolean` |
| `validator` | `Function` | - | 自定义校验函数 `(value, field, mode) => string` |
| `options` | `Array \| Function` | - | 选项数组（用于 `select`, `radio`, `checkbox`） |
| `tableConfig` | `Object` | - | 子表格配置（当 `type` 为 `'table'` 时生效），结构同根配置 |
| `enterFunction` | `Function` | - | 回车后触发的函数 `(field, formData) => void` |
| `attrs` | `Object` | `{}` | 透传给 Arco 组件的属性或事件 |
### 示例

```javascript
columns: [
  {
    title: '姓名',           // 列标题
    dataIndex: 'name',      // 数据字段名
    width: 160,             // 列宽
    visible: true,          // 默认是否显示
    fixed: 'left',          // 固定列 'left' | 'right'
    align: 'left',          // 对齐方式 'left' | 'right' | 'center'
    ellipsis: true,         // 超出省略
    sortable: {             // 排序配置
      compare: (a, b) => a.localeCompare(b)
    },
    slotName: 'name-cell',  // 自定义单元格插槽名（可选）
    // 如果要显示tag，比如状态字段 当columns.[].slotName === 'status-cell' 才生效 这是个预留插槽
    statusMap: {
      active: { label: "在职", color: "green" },
      inactive: { label: "离职", color: "red" },
    },
    // 表单配置（用于新增/编辑弹窗） 没有这个字段就代表弹窗里面不会出现这个字段
    form: {
      type: 'input',        // 控件类型：'input', 'select', 'number', 'date', 'radio', 'switch', 'textarea', 'slot', 'table'
      slotName: 'name-form',// 定义表单项插槽名 （当 `type` 为 `'slot'` 时必填）
      creatable: true,      // 新增时是否显示
      editable: true,       // 编辑时是否显示
      required: true,       // 是否必填
      placeholder: '请输入',
      defaultValue: null,    // 默认值
      enterNext: 'age',     // 回车后跳转到的下一个字段名（提升录入体验）
      oneRow: true,         // 表单是否独占一行
      columns: 0,           // 这个表单项所占列数 0代表不生效
      disabled: ( formData, field ) => formData.status === 'inactive', // 是否禁用可以是函数返回布尔值也可以直接是布尔值
      validator: ( value, field, mode ) => { return '参数不合法' }, // 表单验证的时候使用的校验函数，返回错误信息字符串或空字符串表示通过
      attrs: { },             // 透传给 Arco 组件的属性或者事件
      // type==='select' 或 'radio' 或 'checkbox' 时需要 可以是函数返回数组
      options: [
        { label: '选项A', value: 'A' }
      ],
      tableConfig:{} // 只有 type === 'table' 的时候生效 定义子表格的配置，这个配置是嵌套配置，和最外层的配置是一样的
    }
  }
]
```

### 2.7 搜索配置 (searchFields)

`searchFields` 定义表格顶部的搜索栏。

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `title` | `String` | - | 搜索项标题 |
| `dataIndex` | `String` | - | 对应字段名 |
| `type` | `String` | `'input'` | 类型：`'input'`, `'select'`, `'date'`, `'date-range'`, `'number'`, `'slot'` |
| `slotName` | `String` | - | 自定义搜索插槽名（当 `type` 为 `'slot'` 时必填） |
| `placeholder` | `String` | - | 占位符 |
| `attrs` | `Object` | `{}` | 透传给 Arco 组件的属性或事件 |
| `options` | `Array \| Function` | - | 选项数组（用于 `select`, `radio`, `checkbox`） |

### 示例

```javascript
searchFields: [
  {
    title: '姓名',
    dataIndex: 'name',
    type: 'input',          // 类型：'input', 'select', 'date', 'date-range', 'number', 'slot'
    slotName: 'name-search',// 自定义搜索插槽名（可选）当type==='slot' 的时候必填 
    placeholder: '搜索姓名',
    // 透传给 Arco 组件的属性或者事件
    attrs: {},
    // 只有type === 'select' 或 'radio' 或 'checkbox' 时才生效 可以是函数返回数组
    options: () => [
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ],
  }
]
```

### 2.8 操作按钮 (actions)

`actions` 定义表格左上角的操作按钮。

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `key` | `String` | - | 按钮唯一标识 |
| `label` | `String` | - | 按钮文本 |
| `status` | `String` | - | 按钮状态：`'danger'`, `'warning'`, `'success'`, `'normal'` |
| `icon` | `String` | - | 按钮图标（暂未生效） |
| `type` | `String` | `'secondary'` | 按钮类型：`'primary'`, `'secondary'`, `'dashed'`, `'outline'`, `'text'`。`'confirm'` 等同于 `'outline'` 但带确认框 |
| `confirmMessage` | `String` | - | 二次确认提示语（当 `type` 为 `'confirm'` 时生效） |
| `disabled` | `Boolean \| Function` | `false` | 是否禁用 |
| `isFetchData` | `Boolean` | `true` | 操作完成后是否刷新表格 |
| `needSelect` | `Boolean` | `true` | 是否需要选中行才能操作 |
| `isClearSelect` | `Boolean` | `true` | 操作完成后是否清除选中状态 |
| `apiUrl` | `String` | - | 接口地址（仅在回调中使用） |
| `params` | `Function` | - | 参数处理函数 `(records) => object` |
| `attrs` | `Object` | `{}` | 透传给 Arco 组件的属性或事件 |

### 示例

```javascript
actions: [
  {
    key: 'delete',
    label: '删除',
    status: 'danger',
    icon:'delete',// 图标 暂时不生效
    type: 'confirm',       // 按钮类型 primary - 主要按钮、secondary - 次要按钮（默认）、dashed - 虚线按钮、outline - 线形按钮、text - 文本按钮五种类型。 confirm 和 secondary 一样
    confirmMessage: '确定删除?',// type === 'confirm' 时生效
    disabled:false, // 是否禁用 可以是函数返回布尔值也可以直接是布尔值
    isFetchData: true, // 执行完之后是否刷新数据表格 默认是true
    needSelect:true,// 是否需要选择数据默认为true,为false则不会校验是否选择了数据
    isClearSelect: true, // 需要选择行的时候，执行完之后是否清除选择的行 默认是true
    // 点击后触发 executeAction 回调
    apiUrl:'',// 这个操作的接口地址只是在executeAction 回调中使用，不会自动调用
    params: (records)=>{}, // params 处理方法 第一个参数是选中行数据必须返回一个对象，返回对象会在executeAction 回调中作为参数
    attrs:{}// 透传给 Arco 组件的属性或者事件
  }
]
```

---

## 3. 插槽使用指南

### 3.1 单元格插槽 (Table Cell)

在 `columns` 中指定 `slotName`，自定义单元格渲染。

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `record` | `Object` | - | 当前行数据 |
| `column` | `Object` | - | 当前列配置 |
| `rowIndex` | `Number` | - | 当前行索引 |

### 示例

```html
<!-- 配置 -->
columns: [{ title: '状态', dataIndex: 'status', slotName: 'status-cell' }]

<!-- 模板 -->
<template #status-cell="{ record }">
  <a-tag :color="record.status === 1 ? 'green' : 'red'">
    {{ record.status === 1 ? '启用' : '禁用' }}
  </a-tag>
</template>
```

### 3.2 表单项插槽 (Form Item)

在 `columns[].form` 中指定 `type: 'slot'` 和 `slotName`，自定义表单输入控件。

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `domRef` | `Function` | - | 用于绑定元素引用的函数 |
| `field` | `Object` | - | 当前表单项配置 |
| `formData` | `Object` | - | 表单数据对象 |
| `disabled` | `Boolean` | - | 当前是否禁用 |
| `attrs` | `Object` | - | 透传属性 |
| `handleEnter` | `Function` | - | 处理回车事件的函数 |

### 示例

```html
<!-- 配置 -->
form: { type: 'slot', slotName: 'phone-input', required: true }

<!-- 模板 -->
<template
  #phone-input="{ domRef,field,formData,disabled,attrs,handleEnter }"
>
  <!-- 注意要绑定enter事件用来聚焦到下一个控件 -->
  <a-space :size="8">
    <a-input
      :ref="(ref) => domRef(ref)"
      v-model="formData[field.dataIndex]"
      v-bind="attrs"
      :disabled="disabled"
      :placeholder="field.form.placeholder"
      @keydown.enter="handleEnter"
    />
    <a-button>+</a-button>
  </a-space>
</template>
```

### 3.3 搜索栏插槽 (Search Item)

在 `searchFields` 中指定 `type: 'slot'` 和 `slotName`。

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `field` | `Object` | - | 当前搜索项配置 |
| `state` | `Object` | - | 包含 `searchValues` 的状态对象 |
| `config` | `Object` | - | 全局配置对象 |
| `handleSearch` | `Function` | - | 触发搜索的方法 |

### 示例

```html
<!-- 配置 -->
searchFields: [{ dataIndex: 'custom', type: 'slot', slotName: 'custom-search' }]

<!-- 模板 -->
<template #custom-search="{ field, state, config, handleSearch }">
  <a-input-search
    v-model="state.searchValues[field.dataIndex]"
    :placeholder="field.placeholder || `搜索${field.title}`"
    allow-clear
    :size="config.tableSize || 'small'"
    @search="handleSearch"
    v-bind="field.attrs || {}"
    v-on="field.attrs || {}"
  />
</template>
```

### 3.4 操作按钮插槽 (actions-left)

在操作按钮左侧添加自定义按钮。

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `size` | `String` | - | 当前按钮尺寸 |

### 示例

```html
<template #actions-left="{ size }">
  <a-button :size="size">自定义操作</a-button>
</template>
```

### 3.5 操作按钮插槽 (actions-right)

在操作按钮右侧添加自定义按钮。

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `size` | `String` | - | 当前按钮尺寸 |

### 示例

```html
<template #actions-right="{ size }">
  <a-button :size="size">导出 Excel</a-button>
</template>
```

### 3.6 表格上方插槽 (table-top)

在表格上方添加自定义内容。

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| - | - | - | 无特定作用域参数 |

### 示例

```html
<template #table-top>
  <a-alert>这是一个表格上方插槽示例</a-alert>
</template>
```

### 3.7 表格下方插槽 (table-bottom)

在表格下方添加自定义内容。

| 参数 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| - | - | - | 无特定作用域参数 |

### 示例

```html
<template #table-bottom>
  <a-alert>这是一个表格下方插槽示例</a-alert>
</template>
```

---

## 4. 高级功能说明

### 4.1 数据交互回调

组件通过 props 传递的方法处理业务逻辑：

*   **`pageFetchData(url, params)`**: 后端分页模式下，组件请求数据的回调。
    *   `params`: `{ pageNo, pageSize, searchValues }`
    *   **必须返回**: `Promise`，Resolve 结果需包含 `{ records: [], total: 100 }` 结构。

*   **`handleFormSubmit({ config, mode, data, record })`**: 表单提交回调。
    *   `mode`: `'create'` (新增) | `'edit'` (编辑)
    *   `data`: 表单数据对象
    *   在此处调用后端保存接口，成功后组件会自动刷新列表。

*   **`executeAction(action, records, params)`**: 自定义操作按钮点击回调。
    *   `action`: 按钮配置对象
    *   `records`: 选中的行数据数组
    *   常用于批量删除、审核等操作。

### 4.2 本地持久化 (LocalStorage)

开启 `enableLocalStorage: true` 并设置 `uniqueId` 后，组件会自动保存用户的以下偏好：
*   列的显示/隐藏状态
*   列的顺序 (拖拽排序)
*   列的宽度 (拖拽调整)
*   列是否固定 (fixed)
*   列超长是否显示... (overflowTooltip)

组件加载时会自动合并：`最新本地配置` > `初始本地配置` > `代码默认配置`。
列配置弹窗中提供了“重置”按钮，可一键恢复到初始状态。

---