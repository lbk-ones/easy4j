import React, { useState, useEffect, useMemo } from 'react';
import {
  Modal,
  Button,
  Tabs,
  Form,
  Input,
  Select,
  Switch,
  InputNumber,
  Row,
  Col,
  Card,
  Space,
  Divider,
  List,
  Typography,
  Drawer,
  message,
  Collapse,
  ColorPicker,
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  CopyOutlined,
  EyeOutlined,
  SettingOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import SyntaxHighlighter from 'react-syntax-highlighter';
import dracula from 'react-syntax-highlighter/dist/cjs/styles/hljs/dracula';

const { TabPane } = Tabs;
const { TextArea } = Input;
const { Option } = Select;
const { Text, Title, Paragraph } = Typography;
const { Panel } = Collapse;

// Markdown content embedded directly to ensure availability
const DOC_CONTENT = `
# Vue3 Arco SuperTable 组件文档

基于 Arco Design Vue 封装的高级表格组件，集成了配置化列、搜索、分页、CRUD 表单、本地配置持久化等功能，旨在通过 JSON 配置快速构建标准化的后台管理表格。

## 1. 组件功能概述

\`SuperTable\` 是一个功能强大的数据展示和管理组件。

### 核心功能
*   **配置驱动**：通过 \`config\` 对象完全控制表格行为，无需写大量模板代码。
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
   \`\`\`bash
   npm install arco-vue3-supertable

   pnpm install arco-vue3-supertable
   \`\`\`
2. 引入组件
   \`\`\` javascript
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
   \`\`\`
3. 启动全局配置
   \`\`\`javascript
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

   \`\`\`
`;

const defaultConfig = {
  // Basic
  cnDesc: '超级表格',
  tableSize: 'small',
  rowKey: 'id',
  bordered: true, // Simplified from { cell: true }
  stripe: false,
  hoverable: true,
  columnResizable: true,
  showHeader: true,
  selection: true,
  showColumnConfig: true,
  scroll: { x: 1200, y: null },
  contextMenuEnabled: true,
  showSearchBar: false,
  tableDisabled: false,

  // Form
  showForm: true,
  modalWidth: 1000,
  formLayout: 'horizontal',
  formColumns: 4,
  formColGap: 10,
  formRowGap: 10,

  // Pagination
  paginationType: 'backend',
  pageSize: 10,
  pageSizeOptions: [5, 10, 20, 50],
  pageApiUrl: '',
  formAddApiUrl: '',
  formUpdateApiUrl: '',
  formDeleteApiUrl: '',
  tablePaginationAttrs: {},

  // Style
  hoverColor: '#eef5f8',
  hoverFontColor: '',
  headerBgColor: '#eef5f8',
  headerFontColor: '',
  tableAttrs: {},

  // Storage
  enableLocalStorage: true,
  uniqueId: '',
  userCode: '',

  // Arrays
  columns: [],
  searchFields: [],
  actions: [],
};

const Vue3ArcoSupertable = ({ pageInitData, open, onClose }) => {
  const [config, setConfig] = useState(defaultConfig);
  const [activeTab, setActiveTab] = useState('columns');
  const [docVisible, setDocVisible] = useState(false);

  // Column Editor State
  const [columnDrawerVisible, setColumnDrawerVisible] = useState(false);
  const [currentColumn, setCurrentColumn] = useState(null);
  const [currentColumnIndex, setCurrentColumnIndex] = useState(-1);

  // Search Field Editor State
  const [searchDrawerVisible, setSearchDrawerVisible] = useState(false);
  const [currentSearch, setCurrentSearch] = useState(null);
  const [currentSearchIndex, setCurrentSearchIndex] = useState(-1);

  // Action Editor State
  const [actionDrawerVisible, setActionDrawerVisible] = useState(false);
  const [currentAction, setCurrentAction] = useState(null);
  const [currentActionIndex, setCurrentActionIndex] = useState(-1);

  const [apiUrlPrefix, setApiUrlPrefix] = useState('');
  const [form] = Form.useForm();
  const [columnForm] = Form.useForm();
  const [searchForm] = Form.useForm();
  const [actionForm] = Form.useForm();
  const filteredApiUrls = useMemo(() => {
    if (apiUrlPrefix) {
      return pageInitData.allApiUrl.filter(item => item.url?.startsWith(apiUrlPrefix));
    }
    return pageInitData.allApiUrl;
  }, [apiUrlPrefix, pageInitData.allApiUrl]);
  useEffect(() => {
    if (open && pageInitData) {
      setConfig(prev => {
        const next = { ...prev };
        let hasChange = false;
        if (pageInitData.uniqueId && next.uniqueId !== pageInitData.uniqueId) {
          next.uniqueId = pageInitData.uniqueId;
          hasChange = true;
        }
        if (pageInitData.rowKey && next.rowKey !== pageInitData.rowKey) {
          next.rowKey = pageInitData.rowKey;
          hasChange = true;
        }
        if (pageInitData.cnDesc && next.cnDesc !== pageInitData.cnDesc) {
          next.cnDesc = pageInitData.cnDesc;
          hasChange = true;
        }
        if (pageInitData.actions && next.actions !== pageInitData.actions) {
          next.actions = pageInitData.actions;
          hasChange = true;
        }
        if (pageInitData.pageApiUrl && next.pageApiUrl !== pageInitData.pageApiUrl) {
          next.pageApiUrl = pageInitData.pageApiUrl;
          hasChange = true;
        }
        if (pageInitData.formAddApiUrl && next.formAddApiUrl !== pageInitData.formAddApiUrl) {
          next.formAddApiUrl = pageInitData.formAddApiUrl;
          hasChange = true;
        }
        if (
          pageInitData.formUpdateApiUrl &&
          next.formUpdateApiUrl !== pageInitData.formUpdateApiUrl
        ) {
          next.formUpdateApiUrl = pageInitData.formUpdateApiUrl;
          hasChange = true;
        }
        if (
          pageInitData.formDeleteApiUrl &&
          next.formDeleteApiUrl !== pageInitData.formDeleteApiUrl
        ) {
          next.formDeleteApiUrl = pageInitData.formDeleteApiUrl;
          hasChange = true;
        }
        if (pageInitData.columns.length > 0) {
          next.columns = pageInitData.columns;
          hasChange = true;
        }
        return hasChange ? next : prev;
      });
    }
  }, [open, pageInitData]);

  useEffect(() => {
    if (open) {
      form.setFieldsValue({
        ...config,
        scrollX: config.scroll?.x,
        scrollY: config.scroll?.y,
      });
    }
  }, [open, config, form]);

  const handleValuesChange = (changedValues, allValues) => {
    // Handle nested scroll object separately if needed, or just merge
    const newConfig = { ...config, ...allValues };

    // Always consolidate scroll x/y from form values to config.scroll
    // This ensures config.scroll is always up to date and clean
    if ('scrollX' in allValues || 'scrollY' in allValues) {
      newConfig.scroll = {
        x: allValues.scrollX !== undefined ? allValues.scrollX : config.scroll?.x,
        y: allValues.scrollY !== undefined ? allValues.scrollY : config.scroll?.y,
      };
      // Remove flat keys from config state to keep it clean
      delete newConfig.scrollX;
      delete newConfig.scrollY;
    }

    setConfig(newConfig);
  };

  const getExportConfig = cfg => {
    const newConfig = { ...cfg };
    if (newConfig.bordered === true) {
      newConfig.bordered = { cell: true };
    } else if (newConfig.bordered === false) {
      newConfig.bordered = null;
    }

    // Ensure scroll output format is clean and strictly matches requirements
    if (newConfig.scroll) {
      const { x, y } = newConfig.scroll;
      newConfig.scroll = {
        x: x,
        y: y === null || y === undefined ? 'auto' : y,
      };
    }

    return newConfig;
  };
  const getTemplateResStr = config => {
    let tempStr = JSON.stringify(getExportConfig(config), null, 4)
      .slice(1, -1)
      .trim()
      .split('\n')
      .map((item, index, arr) => {
        let sl = item.replace('"', '').replace('"', '');
        if (index == arr.length - 1) {
          sl = sl + ',';
        }
        return sl;
      })
      .join('\n');
    let temp = `{  
    %temp%
    // 执行操作按钮的回调 edit 和 view 不会进入这个回调 因为它们是弹窗形式的操作
    executeAction: async (config, action, records, params) => {
        // records: 为选中的数据数组
        // action: 为当前这个操作对应的action对象
        // params: action params 方法 处理过后的参数
        let rowKeyName = config.rowKey || "id";
        if (action.key == "delete") {
            if (config.paginationType === "frontend") {
                // 前端分页 删除逻辑
                records.forEach((record) => {
                const index = tableData.value.findIndex(
                    (item) => item[rowKeyName] === record[rowKeyName]
                );
                if (index !== -1) {
                    tableData.value.splice(index, 1);
                }
                });
            } else {
                // 后端分页 删除逻辑
                await del(
                (action.apiUrl || config.formDeleteApiUrl) +
                    "/" +
                    records?.map((r) => r[rowKeyName])?.join(",")
                );
            }
        }
    },
    // API 请求（后端分页）
    pageFetchData: async (url, params, searchFields) => {
        // url 为 apiUrl
        // params 为分页和搜索参数对象
        /**
         params 结构示例
        {
            pageNo: 1,
            pageSize: 10,
            searchValues: [{name:'value'}]
        }
        */
        console.log("后端分页请求参数:", params);
        let keys = [];
        Object.keys(params?.searchValues).forEach((key) => {
        if (
            params.searchValues[key] === null ||
            params.searchValues[key] === undefined ||
            params.searchValues[key] === ""
        ) {
            delete params.searchValues[key];
        } else {
            let findItem = searchFields?.find((item) => item.dataIndex === key);
            keys.push([key, findItem?.condition || "eq", params.searchValues[key]]);
        }
        });
        loading.value = true;
        return post(url, {
            pageQuery: {
                pageNo: params.pageNo,
                pageSize: params.pageSize,
                searchKey: "",
                keys: keys,
            },
        });
    },
    // 表单提交事件 - 处理新增和编辑逻辑
    handleFormSubmit: async ({ config, mode, data, record }) => {
        // config 是这个tableConfig的对象
        // mode 是新增还是编辑或者是其他的 create:新增 edit:编辑
        // data 是表单提交的数据
        // record 是当前编辑的行数据，新增时为 null
        if (mode === "create") {
        // 新增逻辑：调用后端 API 创建数据
        console.log("新增数据:", data);
        if (config.paginationType === "frontend") {
            tableData.value.push({
            [config?.rowKey || "key"]: String(Date.now() + Math.random()),
            ...data,
            });
        } else {
            await post(config.formAddApiUrl, {
                ${pageInitData.controllerReqDtoName}: [data],
            });
        }
        Message.success("新增成功");
        } else if (mode === "edit") {
        // 编辑逻辑：调用后端 API 更新数据
        console.log("编辑数据:", data);
        if (config.paginationType === "frontend") {
            const index = tableData.value.findIndex(
            (item) =>
                item[config?.rowKey || "key"] === data[config?.rowKey || "key"]
            );
            if (index !== -1) {
            tableData.value[index] = {
                ...tableData.value[index],
                ...data,
            };
            }
        } else {
            await put(config.formUpdateApiUrl, {
                ${pageInitData.controllerReqDtoName}: [data],
            });
        }
        Message.success("编辑成功");
        }
    },
    // 搜索条件变更
    handleSearch: (searchValues) => {
        console.log("搜索条件:", searchValues);
    },
    // 分页变化
    handlePageChange: (pagination) => {
        console.log("分页信息:", pagination);
    },
    // 列配置变化
    handleColumnConfigChange: (config) => {
        console.log("列配置变化", config);
    },
}`;
    return temp.replace(/%temp%/g, tempStr);
  };

  const copyToClipboard = () => {
    const jsonString = getTemplateResStr(config);
    const jsString = `const config = reactive(${jsonString});`;
    navigator.clipboard.writeText(jsString).then(() => {
      message.success('配置已复制到剪贴板');
    });
  };
  const copyCodeToClipboard = () => {
    const jsonString = getTemplateResStr(config);
    const prefix = `<script setup>
// 未全局注册和引入则放开注释 
//import { SuperTable } from "arco-vue3-supertable";
//import "arco-vue3-supertable/dist/arco-vue3-supertable.css";
import { ref, reactive } from "vue";
import { post, put, del } from "./request.js";
import { Message } from "@arco-design/web-vue";
// 表格加载状态
const loading = ref(false);
// 数据
const tableData = ref([]);
// 选中数据
const selectedKeys = ref([]);
// columns 的字段的宽度最好不要每个都写死，留一个自动计算，不然fixed会有问题的`;
    const jsString = `const config = reactive(${jsonString});`;
    const suffix = `</script>
<template>
  <SuperTable
    :config="config"
    v-model:data="tableData"
    v-model:loading="loading"
    v-model:selectedKeys="selectedKeys"
  >
  </SuperTable>
</template>
<style scoped>
</style>`;
    navigator.clipboard.writeText(prefix + '\n' + jsString + '\n' + suffix).then(() => {
      message.success('配置已复制到剪贴板');
    });
  };

  // --- Column Management ---
  const openColumnEditor = (record, index) => {
    setCurrentColumn(record || {});
    setCurrentColumnIndex(index);
    // Flatten form config for the form
    const formData = {
      ...record,
      ...record?.form, // Flatten form props to top level for the editor form, will restructure on save
      // Handle form options specifically if it's an array
      formOptions: JSON.stringify(record?.form?.options || []),
      formTableConfig: JSON.stringify(record?.form?.tableConfig || {}),
    };
    columnForm.setFieldsValue(formData);
    setColumnDrawerVisible(true);
  };

  const addRowIndexCell = () => {
    setConfig(prev => {
      let next = { ...prev };
      const index = next.columns.findIndex(i => i.dataIndex === '_rowIndex');
      if (index !== -1) {
        next.columns.splice(index, 1);
      }
      next.columns.unshift({
        title: '序号',
        dataIndex: '_rowIndex',
        width: 70,
        visible: true,
        align: 'left',
      });
      return next;
    });
  };

  const toSearch = (record_, index) => {
    setConfig(prev => {
      let record = JSON.parse(JSON.stringify(record_));
      record.type = record?.form?.type || 'input';
      record.placeholder = record?.form?.placeholder || '请输入';
      let next = { ...prev };
      delete record.visible;
      delete record.width;
      delete record.ellipsis;
      delete record.form;
      next.searchFields = [...next.searchFields, record];
      return next;
    });
  };

  const toVisible = (record, index, e) => {
    setConfig(prev => {
      let next = { ...prev };
      next.columns[index].visible = e;
      return next;
    });
  };

  const saveColumn = async () => {
    try {
      const values = await columnForm.validateFields();
      const {
        // Extract Column props
        title,
        dataIndex,
        width,
        visible,
        fixed,
        align,
        ellipsis,
        sortable,
        slotName,
        statusMap,
        // Extract Form props
        type,
        required,
        creatable,
        editable,
        placeholder,
        enterNext,
        oneRow,
        columns: formCols,
        defaultValue,
        disabled,
        formOptions,
        formTableConfig,
        ...rest
      } = values;

      const newColumn = {
        title,
        dataIndex,
        width,
        visible,
        fixed,
        align,
        ellipsis,
        sortable,
        slotName,
        statusMap,
        form: {
          type,
          required,
          creatable,
          editable,
          placeholder,
          enterNext,
          oneRow,
          columns: formCols,
          defaultValue,
          disabled,
          options: formOptions ? JSON.parse(formOptions) : undefined,
          tableConfig: formTableConfig ? JSON.parse(formTableConfig) : undefined,
        },
      };

      const newColumns = [...config.columns];
      if (currentColumnIndex > -1) {
        newColumns[currentColumnIndex] = newColumn;
      } else {
        newColumns.push(newColumn);
      }
      setConfig({ ...config, columns: newColumns });
      setColumnDrawerVisible(false);
    } catch (e) {
      console.error(e);
    }
  };

  const deleteColumn = index => {
    if (config.columns.length <= index) {
      return;
    }

    const newColumns = [...config.columns];
    let column = newColumns.splice(index, 1);

    let searchFields =
      config?.searchFields?.filter(item => item.dataIndex !== column?.[0]?.dataIndex) ?? [];

    if (index > 0) {
      let pref = newColumns[index - 1];
      if (pref.form && newColumns.length > index) {
        pref.form.enterNext = newColumns[index].dataIndex;
      }
    }
    setConfig({ ...config, columns: newColumns, searchFields });
  };

  // --- Search Field Management ---
  const openSearchEditor = (record, index) => {
    setCurrentSearch(record || {});
    setCurrentSearchIndex(index);
    searchForm.setFieldsValue({
      ...record,
      searchOptions: JSON.stringify(record?.options || []),
    });
    setSearchDrawerVisible(true);
  };

  const saveSearch = async () => {
    try {
      const values = await searchForm.validateFields();
      const { searchOptions, ...rest } = values;
      const newSearch = {
        ...rest,
        options: searchOptions ? JSON.parse(searchOptions) : undefined,
      };

      const newSearchFields = [...config.searchFields];
      if (currentSearchIndex > -1) {
        newSearchFields[currentSearchIndex] = newSearch;
      } else {
        newSearchFields.push(newSearch);
      }
      setConfig({ ...config, searchFields: newSearchFields });
      setSearchDrawerVisible(false);
    } catch (e) {
      console.error(e);
    }
  };

  const deleteSearch = index => {
    const newFields = [...config.searchFields];
    newFields.splice(index, 1);
    setConfig({ ...config, searchFields: newFields });
  };

  // --- Action Management ---
  const openActionEditor = (record, index) => {
    setCurrentAction(record || {});
    setCurrentActionIndex(index);
    actionForm.setFieldsValue(record);
    setActionDrawerVisible(true);
  };

  const saveAction = async () => {
    try {
      const values = await actionForm.validateFields();
      const newActions = [...config.actions];
      if (currentActionIndex > -1) {
        newActions[currentActionIndex] = values;
      } else {
        newActions.push(values);
      }
      setConfig({ ...config, actions: newActions });
      setActionDrawerVisible(false);
    } catch (e) {
      console.error(e);
    }
  };

  const deleteAction = index => {
    const newActions = [...config.actions];
    newActions.splice(index, 1);
    setConfig({ ...config, actions: newActions });
  };

  return (
    <Drawer
      title='Vue3 Arco SuperTable 配置生成器'
      open={open}
      onClose={onClose}
      width='100%'
      style={{ top: 0, padding: 0 }}
      //bodyStyle={{ height: 'calc(100vh - 110px)', overflow: 'hidden', padding: 0 }}
      footer={null}
    >
      <div style={{ display: 'flex', height: '100%' }}>
        {/* Left Panel: Configuration Form */}
        <div
          style={{
            flex: 1,
            padding: '20px',
            overflowY: 'auto',
            borderRight: '1px solid #f0f0f0',
          }}
        >
          <div style={{ marginBottom: 16 }}>
            <Button type='primary' onClick={() => setDocVisible(true)} icon={<EyeOutlined />}>
              查看使用文档
            </Button>
            <Button style={{ marginLeft: 8 }} onClick={() => setConfig(defaultConfig)}>
              重置配置
            </Button>
          </div>

          <Form
            form={form}
            layout='vertical'
            onValuesChange={handleValuesChange}
            initialValues={config}
          >
            <Tabs activeKey={activeTab} onChange={setActiveTab}>
              <TabPane tab='基础配置' key='basic'>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name='cnDesc' label='表格标题'>
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      noStyle
                      shouldUpdate={(prev, current) =>
                        prev.enableLocalStorage !== current.enableLocalStorage
                      }
                    >
                      {({ getFieldValue }) => (
                        <Form.Item
                          name='uniqueId'
                          label='唯一标识(LocalStorage)'
                          rules={[
                            {
                              required: getFieldValue('enableLocalStorage'),
                              message: '开启本地存储时，唯一标识必填',
                            },
                          ]}
                        >
                          <Input />
                        </Form.Item>
                      )}
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item name='tableSize' label='表格密度'>
                      <Select>
                        <Option value='mini'>Mini</Option>
                        <Option value='small'>Small</Option>
                        <Option value='medium'>Medium</Option>
                        <Option value='large'>Large</Option>
                      </Select>
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item name='rowKey' label='主键字段'>
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item name='userCode' label='用户标识'>
                      <Input />
                    </Form.Item>
                  </Col>
                </Row>
                <Divider orientation='left'>开关配置</Divider>
                <Row gutter={16}>
                  <Col span={6}>
                    <Form.Item name='bordered' label='显示边框' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name='stripe' label='斑马纹' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name='hoverable' label='悬停效果' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name='columnResizable' label='列宽拖拽' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name='showHeader' label='显示表头' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name='selection' label='显示多选' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name='showColumnConfig' label='列设置按钮' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item
                      name='enableLocalStorage'
                      label='本地存储配置'
                      valuePropName='checked'
                    >
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name='contextMenuEnabled' label='右键菜单' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name='showSearchBar' label='总是显示搜索' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name='tableDisabled' label='禁用表格' valuePropName='checked'>
                      <Switch />
                    </Form.Item>
                  </Col>
                </Row>
                <Divider orientation='left'>滚动配置</Divider>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name='scrollX' label='横向滚动(x)' initialValue={1200}>
                      <InputNumber style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name='scrollY' label='纵向滚动(y)' initialValue={null}>
                      <InputNumber style={{ width: '100%' }} placeholder='Auto' />
                    </Form.Item>
                  </Col>
                </Row>
              </TabPane>

              <TabPane tab='表单配置' key='form'>
                <Form.Item name='showForm' label='启用内置CRUD表单' valuePropName='checked'>
                  <Switch />
                </Form.Item>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name='modalWidth' label='弹窗宽度'>
                      <InputNumber style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name='formLayout' label='表单布局'>
                      <Select>
                        <Option value='horizontal'>Horizontal</Option>
                        <Option value='vertical'>Vertical</Option>
                        <Option value='inline'>Inline</Option>
                      </Select>
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item name='formColumns' label='每行列数'>
                      <InputNumber style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item name='formColGap' label='列间距'>
                      <InputNumber style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item name='formRowGap' label='行间距'>
                      <InputNumber style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                </Row>
              </TabPane>

              <TabPane tab='分页 & API' key='pagination'>
                <Form.Item name='paginationType' label='分页模式'>
                  <Select>
                    <Option value='frontend'>前端分页</Option>
                    <Option value='backend'>后端分页</Option>
                  </Select>
                </Form.Item>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name='pageSize' label='每页条数'>
                      <InputNumber style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  {/* Simplified pageSizeOptions handling */}
                </Row>
                <Divider orientation='left'>API 地址</Divider>
                <Input
                  style={{ width: '30%', margin: '10px 0' }}
                  placeholder='请输入API地址前缀进行过滤'
                  value={apiUrlPrefix}
                  onChange={e => setApiUrlPrefix(e.target.value)}
                />
                <Form.Item name='pageApiUrl' label='列表数据 API'>
                  <Select
                    showSearch={true}
                    options={filteredApiUrls.map(item => ({
                      value: item.url,
                      label: `${item.url} 【${item.summary || ''}】-【${item.description || ''}】`,
                    }))}
                  ></Select>
                </Form.Item>
                <Form.Item name='formAddApiUrl' label='新增 API'>
                  <Select
                    showSearch={true}
                    options={filteredApiUrls.map(item => ({
                      value: item.url,
                      label: `${item.url} 【${item.summary || ''}】-【${item.description || ''}】`,
                    }))}
                  ></Select>
                </Form.Item>
                <Form.Item name='formUpdateApiUrl' label='更新 API'>
                  <Select
                    showSearch={true}
                    options={filteredApiUrls.map(item => ({
                      value: item.url,
                      label: `${item.url} 【${item.summary || ''}】-【${item.description || ''}】`,
                    }))}
                  ></Select>
                </Form.Item>
                <Form.Item name='formDeleteApiUrl' label='删除 API'>
                  <Select
                    showSearch={true}
                    options={filteredApiUrls.map(item => ({
                      value: item.url,
                      label: `${item.url} 【${item.summary || ''}】-【${item.description || ''}】`,
                    }))}
                  ></Select>
                </Form.Item>
              </TabPane>

              <TabPane tab='样式' key='style'>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name='hoverColor' label='悬停背景色'>
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name='headerBgColor' label='表头背景色'>
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name='hoverFontColor' label='悬停文字色'>
                      <Input />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name='headerFontColor' label='表头文字色'>
                      <Input />
                    </Form.Item>
                  </Col>
                </Row>
              </TabPane>

              <TabPane tab={`列配置【${config.columns.length}】`} key='columns'>
                <div style={{ display: 'flex', gap: '10px', justifyContent: 'space-between' }}>
                  <Button
                    type='dashed'
                    onClick={() => openColumnEditor({}, -1)}
                    block
                    icon={<PlusOutlined />}
                  >
                    添加列
                  </Button>
                  <Button type='dashed' onClick={() => addRowIndexCell()} icon={<PlusOutlined />}>
                    添加序号列
                  </Button>
                </div>

                <List
                  style={{ marginTop: 10 }}
                  bordered
                  dataSource={config.columns}
                  renderItem={(item, index) => (
                    <List.Item
                      actions={[
                        <label
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '5px',
                          }}
                        >
                          <span>是否显示:</span>
                          <Switch
                            checked={
                              config.columns[index].visible == void 0
                                ? true
                                : config.columns[index].visible
                            }
                            onChange={e => toVisible(item, index, e)}
                          />
                        </label>,
                        <Button
                          type='text'
                          disabled={config.searchFields?.some(
                            searchItem => searchItem.dataIndex === item.dataIndex
                          )}
                          icon={<SearchOutlined />}
                          onClick={() => toSearch(item, index)}
                        >
                          设为搜索
                        </Button>,
                        <Button
                          type='text'
                          icon={<SettingOutlined />}
                          onClick={() => openColumnEditor(item, index)}
                        >
                          编辑
                        </Button>,
                        <Button
                          type='text'
                          danger
                          icon={<DeleteOutlined />}
                          onClick={() => deleteColumn(index)}
                        >
                          删除
                        </Button>,
                      ]}
                    >
                      <List.Item.Meta
                        title={item.title || '未命名列'}
                        description={`Field: ${item.dataIndex} | Width: ${
                          item.width || 'Auto'
                        } | Form: ${item.form?.type || 'None'}`}
                      />
                    </List.Item>
                  )}
                />
              </TabPane>

              <TabPane tab={`搜索【${config.searchFields.length}】`} key='search'>
                <Button
                  type='dashed'
                  onClick={() => openSearchEditor({}, -1)}
                  block
                  icon={<PlusOutlined />}
                >
                  添加搜索项
                </Button>
                <List
                  style={{ marginTop: 10 }}
                  bordered
                  dataSource={config.searchFields}
                  renderItem={(item, index) => (
                    <List.Item
                      actions={[
                        <Button
                          type='text'
                          icon={<SettingOutlined />}
                          onClick={() => openSearchEditor(item, index)}
                        >
                          编辑
                        </Button>,
                        <Button
                          type='text'
                          danger
                          icon={<DeleteOutlined />}
                          onClick={() => deleteSearch(index)}
                        >
                          删除
                        </Button>,
                      ]}
                    >
                      <List.Item.Meta
                        title={item.title || '未命名'}
                        description={`Field: ${item.dataIndex} | Type: ${item.type}`}
                      />
                    </List.Item>
                  )}
                />
              </TabPane>

              <TabPane tab={`按钮【${config.actions.length}】`} key='actions'>
                <Button
                  type='dashed'
                  onClick={() => openActionEditor({}, -1)}
                  block
                  icon={<PlusOutlined />}
                >
                  添加按钮
                </Button>
                <List
                  style={{ marginTop: 10 }}
                  bordered
                  dataSource={config.actions}
                  renderItem={(item, index) => (
                    <List.Item
                      actions={[
                        <Button
                          type='text'
                          icon={<SettingOutlined />}
                          onClick={() => openActionEditor(item, index)}
                        >
                          编辑
                        </Button>,
                        <Button
                          type='text'
                          danger
                          icon={<DeleteOutlined />}
                          onClick={() => deleteAction(index)}
                        >
                          删除
                        </Button>,
                      ]}
                    >
                      <List.Item.Meta
                        title={item.label || item.key}
                        description={`Key: ${item.key} | Type: ${item.type || 'secondary'}`}
                      />
                    </List.Item>
                  )}
                />
              </TabPane>
            </Tabs>
          </Form>
        </div>

        {/* Right Panel: Preview */}
        <div
          style={{
            width: '40%',
            padding: '20px',
            backgroundColor: '#fafafa',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              marginBottom: 16,
              alignItems: 'center',
            }}
          >
            <Title level={4} style={{ margin: 0 }}>
              配置预览 (JSON)
            </Title>
            <div style={{ display: 'flex', gap: '10px' }}>
              <Button type='primary' icon={<CopyOutlined />} onClick={copyToClipboard}>
                复制 JS 配置
              </Button>
              <Button type='primary' icon={<CopyOutlined />} onClick={copyCodeToClipboard}>
                复制代码
              </Button>
            </div>
          </div>
          <TextArea
            value={getTemplateResStr(config)}
            autoSize={false}
            style={{
              flex: 1,
              fontFamily: 'monospace',
              fontSize: '16px',
              whiteSpace: 'pre',
            }}
            readOnly
          />
        </div>
      </div>

      {/* Document Drawer */}
      <Drawer
        title='组件文档'
        placement='right'
        width={'50%'}
        onClose={() => setDocVisible(false)}
        open={docVisible}
      >
        {/* <div style={{ whiteSpace: 'pre-wrap' }}>
                    {DOC_CONTENT}
                </div> */}

        <div
          style={{
            padding: '20px',
            maxWidth: '800px',
            margin: '0 auto',
            fontSize: '16px',
            lineHeight: '24px',
          }}
        >
          <ReactMarkdown
            // 自定义代码块渲染（实现语法高亮）
            components={{
              code({ node, inline, className, children, ...props }) {
                const match = /language-(\w+)/.exec(className || '');
                return !inline && match ? (
                  <SyntaxHighlighter style={dracula} language={match[1]} PreTag='div' {...props}>
                    {String(children).replace(/\n$/, '')}
                  </SyntaxHighlighter>
                ) : (
                  <code className={className} {...props}>
                    {children}
                  </code>
                );
              },
            }}
          >
            {DOC_CONTENT}
          </ReactMarkdown>
        </div>
      </Drawer>

      {/* Column Editor Drawer */}
      <Drawer
        title={currentColumnIndex > -1 ? '编辑列' : '添加列'}
        width={'50%'}
        onClose={() => setColumnDrawerVisible(false)}
        open={columnDrawerVisible}
        extra={
          <Button type='primary' onClick={saveColumn}>
            保存
          </Button>
        }
      >
        <Form form={columnForm} layout='vertical'>
          <Tabs defaultActiveKey='col-basic'>
            <TabPane tab='列基础' key='col-basic'>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name='title' label='标题' rules={[{ required: true }]}>
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name='dataIndex' label='字段名' rules={[{ required: true }]}>
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item name='width' label='宽度'>
                    <InputNumber style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item name='align' label='对齐'>
                    <Select>
                      <Option value='left'>Left</Option>
                      <Option value='center'>Center</Option>
                      <Option value='right'>Right</Option>
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item name='fixed' label='固定'>
                    <Select allowClear>
                      <Option value='left'>Left</Option>
                      <Option value='right'>Right</Option>
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item
                    name='visible'
                    label='显示'
                    valuePropName='checked'
                    initialValue={true}
                  >
                    <Switch />
                  </Form.Item>
                </Col>
                <Col span={6}>
                  <Form.Item name='ellipsis' label='超出文本省略' valuePropName='checked'>
                    <Switch />
                  </Form.Item>
                </Col>
                <Form.Item noStyle shouldUpdate={(prev, current) => prev.type !== current.type}>
                  {({ getFieldValue }) =>
                    getFieldValue('type') !== 'table' ? (
                      <Col span={12}>
                        <Form.Item name='slotName' label='自定义插槽'>
                          <Input />
                        </Form.Item>
                      </Col>
                    ) : null
                  }
                </Form.Item>
              </Row>
            </TabPane>
            <TabPane tab='表单配置' key='col-form'>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name='type' label='控件类型' initialValue='input'>
                    <Select>
                      <Option value='input'>Input</Option>
                      <Option value='select'>Select</Option>
                      <Option value='number'>Number</Option>
                      <Option value='date'>Date</Option>
                      <Option value='time'>Time</Option>
                      <Option value='datetime'>DateTime</Option>
                      <Option value='radio'>Radio</Option>
                      <Option value='switch'>Switch</Option>
                      <Option value='textarea'>Textarea</Option>
                      <Option value='slot'>Slot</Option>
                      <Option value='table'>SubTable</Option>
                    </Select>
                  </Form.Item>
                </Col>
                <Form.Item noStyle shouldUpdate={(prev, current) => prev.type !== current.type}>
                  {({ getFieldValue }) => {
                    const isTable = getFieldValue('type') === 'table';
                    return (
                      <>
                        <Col span={12}>
                          <Form.Item
                            noStyle
                            shouldUpdate={(prev, current) => prev.type !== current.type}
                          >
                            {({ getFieldValue }) => (
                              <Form.Item
                                name='formSlotName'
                                label='自定义表单插槽'
                                rules={[
                                  {
                                    required: getFieldValue('type') === 'slot',
                                    message: '当类型为Slot时，插槽名必填',
                                  },
                                ]}
                              >
                                <Input placeholder='输入插槽名称' disabled={isTable} />
                              </Form.Item>
                            )}
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name='placeholder' label='占位符'>
                            <Input disabled={isTable} />
                          </Form.Item>
                        </Col>
                        <Col span={6}>
                          <Form.Item
                            name='creatable'
                            label='新增显示'
                            valuePropName='checked'
                            initialValue={true}
                          >
                            <Switch disabled={isTable} />
                          </Form.Item>
                        </Col>
                        <Col span={6}>
                          <Form.Item
                            name='editable'
                            label='编辑显示'
                            valuePropName='checked'
                            initialValue={true}
                          >
                            <Switch disabled={isTable} />
                          </Form.Item>
                        </Col>
                        <Col span={6}>
                          <Form.Item name='required' label='必填' valuePropName='checked'>
                            <Switch disabled={isTable} />
                          </Form.Item>
                        </Col>
                        <Col span={6}>
                          <Form.Item name='oneRow' label='独占一行' valuePropName='checked'>
                            <Switch disabled={isTable} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name='enterNext' label='回车跳转字段'>
                            <Select allowClear showSearch disabled={isTable}>
                              {config.columns
                                .filter(c => c.dataIndex !== currentColumn?.dataIndex)
                                .map(c => (
                                  <Option key={c.dataIndex} value={c.dataIndex}>
                                    {c.title || c.dataIndex}
                                  </Option>
                                ))}
                            </Select>
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name='defaultValue' label='默认值'>
                            <Input disabled={isTable} />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item name='formOptions' label='选项 (JSON Array for Select/Radio)'>
                            <TextArea
                              rows={3}
                              placeholder='[{"label":"A","value":1}]'
                              disabled={isTable}
                            />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item name='disabled' label='禁用 (Boolean)' valuePropName='checked'>
                            <Switch disabled={isTable} />
                          </Form.Item>
                        </Col>
                      </>
                    );
                  }}
                </Form.Item>
              </Row>
            </TabPane>
          </Tabs>
        </Form>
      </Drawer>

      {/* Search Editor Drawer */}
      <Drawer
        title={currentSearchIndex > -1 ? '编辑搜索项' : '添加搜索项'}
        width={'70%'}
        onClose={() => setSearchDrawerVisible(false)}
        open={searchDrawerVisible}
        extra={
          <Button type='primary' onClick={saveSearch}>
            保存
          </Button>
        }
      >
        <Form form={searchForm} layout='vertical'>
          <Form.Item name='title' label='标题' rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name='dataIndex' label='字段名' rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name='type' label='类型' initialValue='input'>
            <Select>
              <Option value='input'>Input</Option>
              <Option value='select'>Select</Option>
              <Option value='date'>Date</Option>
              <Option value='date-range'>Date Range</Option>
              <Option value='number'>Number</Option>
              <Option value='slot'>Slot</Option>
            </Select>
          </Form.Item>
          <Form.Item name='condition' label='条件' initialValue='eq'>
            <Select>
              <Option value='eq'>等于</Option>
              <Option value='ne'>不等于</Option>
              <Option value='in'>在范围内</Option>
              <Option value='like'>模糊查询 (LIKE)</Option>
              <Option value='notLike'>不相似（NOT LIKE）</Option>
              <Option value='likeLeft'>左模糊查询 (LIKE LEFT)</Option>
              <Option value='likeRight'>右模糊查询 (LIKE RIGHT)</Option>
              <Option value='lt'>小于</Option>
              <Option value='le'>小于等于</Option>
              <Option value='gt'>大于</Option>
              <Option value='ge'>大于等于</Option>
              <Option value='tgt'>时间大于</Option>
              <Option value='tge'>时间大于等于</Option>  
              <Option value='tlt'>时间小于</Option>  
              <Option value='tle'>时间小于等于</Option>  
              <Option value='between'>时间范围</Option>     
              <Option value='betweene'>时间不在这个范围</Option>     
            </Select>
          </Form.Item>
          <Form.Item name='slotName' label='自定义插槽'>
            <Input />
          </Form.Item>
          <Form.Item name='placeholder' label='占位符'>
            <Input />
          </Form.Item>
          <Form.Item name='searchOptions' label='选项 (JSON Array)'>
            <TextArea rows={3} />
          </Form.Item>
        </Form>
      </Drawer>

      {/* Action Editor Drawer */}
      <Drawer
        title={currentActionIndex > -1 ? '编辑按钮' : '添加按钮'}
        width={'50%'}
        onClose={() => setActionDrawerVisible(false)}
        open={actionDrawerVisible}
        extra={
          <Button type='primary' onClick={saveAction}>
            保存
          </Button>
        }
      >
        <Form form={actionForm} layout='vertical'>
          <Form.Item name='key' label='唯一标识 (Key)' rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name='label' label='按钮文本' rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name='slotName' label='自定义插槽 (可选)'>
            <Input />
          </Form.Item>
          <Form.Item name='type' label='类型' initialValue='secondary'>
            <Select>
              <Option value='primary'>Primary</Option>
              <Option value='secondary'>Secondary</Option>
              <Option value='dashed'>Dashed</Option>
              <Option value='outline'>Outline</Option>
              <Option value='text'>Text</Option>
              <Option value='confirm'>Confirm</Option>
            </Select>
          </Form.Item>
          <Form.Item name='status' label='状态'>
            <Select allowClear>
              <Option value='danger'>Danger</Option>
              <Option value='warning'>Warning</Option>
              <Option value='success'>Success</Option>
            </Select>
          </Form.Item>
          <Form.Item name='icon' label='图标'>
            <Input />
          </Form.Item>
          <Form.Item name='confirmMessage' label='确认提示语'>
            <Input />
          </Form.Item>
          <Form.Item
            name='isFetchData'
            label='完成后刷新'
            valuePropName='checked'
            initialValue={true}
          >
            <Switch />
          </Form.Item>
          <Form.Item name='needSelect' label='需选中行' valuePropName='checked' initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item
            name='isClearSelect'
            label='完成后清除选中'
            valuePropName='checked'
            initialValue={true}
          >
            <Switch />
          </Form.Item>
        </Form>
      </Drawer>
    </Drawer>
  );
};

export default Vue3ArcoSupertable;
