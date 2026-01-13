import { useState, useEffect, useRef } from 'react';
import {
  Layout,
  Card,
  message,
  Form,
  Input,
  Checkbox,
  Button,
  Select,
  Tabs,
  Table,
  Space,
  Typography,
  Divider,
  Row,
  Col,
  Drawer,
  Modal,
  List,
  Flex,
  Alert,
  ColorPicker,
  ConfigProvider,
} from 'antd';
import { CopyOutlined, QuestionCircleOutlined, WarningOutlined } from '@ant-design/icons';
import './App.css';
import { post } from './request.js';
import { cloneDeep, isEmpty } from 'lodash-es';

// 导入 highlight.js 核心
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vs } from 'react-syntax-highlighter/dist/esm/styles/prism/index.js';
import { useColor } from './color.jsx';
import Vue3ArcoSupertable from './Vue3ArcoSupertable.jsx';
import { Switch } from 'antd';

const { Header, Content, Footer } = Layout;
const { Title, Text } = Typography;
const { Option } = Select;

const { TabPane } = Tabs;

function App() {
  let { primaryColor, setPrimaryColor } = useColor();
  // 表单实例
  const [form] = Form.useForm();
  const [modalForm] = Form.useForm();
  const [modal, contextHolder] = Modal.useModal();
  // 选项卡状态
  const [activeTab, setActiveTab] = useState('standard');
  const [activeTab2, setActiveTab2] = useState('');

  const [initData, setInitData] = useState({
    url: '',
    username: '',
    password: '',
    tablePrefix: '',
    exclude: [],
    removeTablePrefix: '',
    parentPackageName: '',
    projectAbsolutePath: '',
    urlPrefix: '',
    deleteIfExists: false,
    headerDesc: '',
    author: '',
    forceDelete: false,
    entityPackageName: '',
    controllerPackageName: '',
    controllerReqPackageName: '',
    dtoPackageName: '',
    mapperPackageName: '',
    mapperXmlPackageName: '',
    serviceInterfacePackageName: '',
    serviceImplPackageName: '',
    allTables: [''],
    genController: false,
    genControllerReq: false,
    genDto: false,
    genEntity: false,
    genMapper: false,
    genMapperXml: false,
    genService: false,
    genServiceImpl: false,
    genMapStruct: false,
  });

  const [previewText, setPreviewText] = useState({});

  const [currentItem, setCurrentItem] = useState({});

  const [open, setOpen] = useState(false);
  const [superTableVisible, setSuperTableVisible] = useState(false);
  const [displayTables, setDisplayTables] = useState(false);
  const [urlPrefixRequired, setUrlPrefixRequired] = useState(false);

  // 1. 创建 ref 指向代码块 DOM
  const codeRef = useRef(null);

  const [scanPackages, setScanPackages] = useState({
    allDtos: [],
    allEntitys: [],
    allControllers: [],
  });

  const [pageInitData, setPageInitData] = useState({
    uniqueId: '',
    cnDesc: '',
    rowKey: '',
    allApiUrl: [],
    columns: [],
  });

  const [modalVisible, setModalVisible] = useState(false);

  const showDrawer = () => {
    setOpen(true);
  };
  const onClose = () => {
    setOpen(false);
  };
  useEffect(() => {
    post('/init', {}).then(res => {
      res.exclude = [];
      setInitData(res);
      form.setFieldsValue(res); // 动态赋值
    });

    // 可选：清理逻辑（按需添加）
    return () => {
      // 清理代码
    };
  }, []);

  // 输出状态
  const [outputCode, setOutputCode] = useState('// 生成的代码将显示在这里...');
  const [isGenerating, setIsGenerating] = useState(false);

  // 更新生成类型
  const handleUpdateGenerateType = (type, value) => {
    if (type === 'genController' || type === 'genControllerReq') {
      let newD = { ...initData, genController: value, genControllerReq: value };
      setInitData(prev => newD);
      if (value === true) {
        setUrlPrefixRequired(true);
      }
      if (newD.genController === false && newD.genControllerReq === false) {
        setUrlPrefixRequired(false);
        form.resetFields(['urlPrefix']);
      }
    } else if (type === 'genMapStruct' && value === true) {
      let newD = { ...initData, genMapStruct: true, genDto: true, genEntity: true };
      setInitData(prev => newD);
    } else if (type === 'genServiceImpl' && value === true && activeTab !== 'custom') {
      let newD = {
        ...initData,
        genServiceImpl: true,
        genDto: true,
        genEntity: true,
        genMapStruct: true,
      };
      setInitData(prev => newD);
    } else {
      setInitData(prev => ({ ...initData, [type]: value }));
    }
  };

  const waringGen = () => {
    return new Promise((resolve, reject) => {
      modal.confirm({
        title: '提示',
        icon: <WarningOutlined />,
        content: '该操作会写入本地文件，请确认!',
        okText: '继续',
        cancelText: '中断',
        onOk() {
          resolve(true);
        },
        onCancel() {
          resolve(false);
        },
      });
    });
  };

  const waring = (data = {}) => {
    return new Promise((resolve, reject) => {
      if (activeTab === 'custom') {
        resolve(true);
        return;
      }
      if (isEmpty(data.tablePrefix)) {
        modal.confirm({
          title: '提示',
          icon: <WarningOutlined />,
          content: '如果不设置表前缀，可能会导致表太多而生成失败!',
          okText: '继续',
          cancelText: '中断',
          onOk() {
            resolve(true);
          },
          onCancel() {
            resolve(false);
          },
        });
      } else {
        resolve(true);
      }
    });
  };
  const scanPackageFunc = () => {
    post(
      '/db/scanPackage',
      {
        projectAbsolutePath: initData.projectAbsolutePath,
        parentPackageName: initData.parentPackageName,
        dtoPackageName: initData.dtoPackageName,
        entityPackageName: initData.entityPackageName,
        controllerPackageName: initData.controllerPackageName,
      },
      { skipLoading: true }
    ).then(res => {
      clearAllCheckBox();
      setScanPackages(res);
    });
  };

  const frontPageInit = (dtoName, domainName,controllerName) => {
    let param = {
      parentPackageName: initData.parentPackageName,
      controllerPackageName: initData.controllerPackageName,
      projectAbsolutePath: initData.projectAbsolutePath,
      dtoPackageName: initData.dtoPackageName,
      entityPackageName: initData.entityPackageName,
      dtoName: dtoName,
      domainName: domainName,
      controllerName: controllerName,
      isEnabledName: form.getFieldValue('isEnabledName'),
      isDeletedName: form.getFieldValue('isDeletedName'),
      isEnabledValid: form.getFieldValue('isEnabledValid'),
      isEnabledNotValid: form.getFieldValue('isEnabledNotValid'),
    };
    post('/pageGenInit', param).then(res => {
      setPageInitData(res);
      setSuperTableVisible(true);
      setModalVisible(false);
      modalForm.resetFields();
    });
  };
  const clearAllCheckBox = () => {
    setInitData({
      ...initData,
      genController: false,
      genControllerReq: false,
      genDto: false,
      genEntity: false,
      genMapper: false,
      genMapperXml: false,
      genService: false,
      genServiceImpl: false,
      genMapStruct: false,
    });
  };

  const hanlderFieldNameIsStr = (obj)=>{
    let isEnabledIsNumber = obj.isEnabledIsNumber;
    let isDeletedIsNumber = obj.isDeletedIsNumber;
    let isEnabledValid = obj.isEnabledValid;
    let isEnabledNotValid = obj.isEnabledNotValid;
      let isDeletedValid = obj.isDeletedValid;
      let isDeletedNotValid = obj.isDeletedNotValid;
      if(isEnabledIsNumber !== true){
          obj.isEnabledValid = `"${isEnabledValid}"`;
          obj.isEnabledNotValid = `"${isEnabledNotValid}"`;
      }
      if(isDeletedIsNumber !== true){
          obj.isDeletedValid = `"${isDeletedValid}"`;
          obj.isDeletedNotValid = `"${isDeletedNotValid}"`;
      }

  }


  return (
    <Layout className='app-layout'>
      {contextHolder}
      <Header className='app-header'>
        <Title level={2} className='header-title'>
          Easy4j底座代码生成工具
        </Title>

        <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
          主题色:
          <ColorPicker
            value={primaryColor}
            onChange={e => {
              setPrimaryColor(e.toHexString());
            }}
          />
        </span>
      </Header>

      <Content className='app-content'>
        <Tabs
          activeKey={activeTab}
          onChange={e => {
            setActiveTab(e);
            if (e === 'custom') {
              scanPackageFunc();
            }
            form.resetFields();
          }}
          tabPlacement={'start'}
          tabBarStyle={{ backgroundColor: 'white' }}
        >
          <TabPane tab='从数据库中生成' key='standard' children={null}></TabPane>
          <TabPane tab='自定义生成' key='custom' children={null}></TabPane>
        </Tabs>

        <div className='standard-form'>
          <span>
            <Space>
              <Button
                type='primary'
                size='middle'
                onClick={async e => {
                  const formValues = await form.validateFields();
                  let parse = cloneDeep({
                    ...initData,
                    ...formValues,
                  });
                  parse.allTables = null;
                  parse.exclude = parse?.exclude?.map(e => e.value)?.join(',');
                  let res = await waring(parse);
                  if (res) {
                     hanlderFieldNameIsStr(parse);
                    if (activeTab === 'custom') {
                      post(
                        '/custom/gen',
                        {
                          ...parse,
                          isPreview: true,
                        },
                        { loadingText: '正在自定义生成预览' }
                      ).then(res => {
                        setPreviewText(res);
                        setActiveTab2(res?.infoList?.[0]?.tagName);
                        setCurrentItem(res?.infoList?.[0]?.itemList?.[0]);
                        showDrawer();
                      });
                    } else {
                      post('/db/preview', parse, { loadingText: '正在生成预览' }).then(res => {
                        setPreviewText(res);
                        setActiveTab2(res?.infoList?.[0]?.tagName);
                        setCurrentItem(res?.infoList?.[0]?.itemList?.[0]);
                        showDrawer();
                      });
                    }
                  }
                }}
              >
                预览代码
              </Button>
              <Button
                type={'primary'}
                size='middle'
                loading={isGenerating}
                onClick={async e => {
                  const formValues = await form.validateFields();
                  let parse = cloneDeep({
                    ...initData,
                    ...formValues,
                  });
                  parse.allTables = null;
                  parse.exclude = parse?.exclude?.map(e => e.value)?.join(',');
                  let res = await waring(parse);
                  if (res) {
                    let gen = await waringGen();
                    if (gen) {
                      hanlderFieldNameIsStr(parse);
                      if (activeTab === 'custom') {
                        post(
                          '/custom/gen',
                          {
                            ...parse,
                            isPreview: false,
                          },
                          { loadingText: '正在自定义生成预览' }
                        ).then(res => {
                          setPreviewText(res);
                          setActiveTab2(res?.infoList?.[0]?.tagName);
                          setCurrentItem(res?.infoList?.[0]?.itemList?.[0]);
                          showDrawer();
                        });
                      } else {
                        post('/db/gen', parse, {
                          loadingText: '正在生成，生成之后会写入文件',
                        }).then(res => {
                          setPreviewText(res);
                          setActiveTab2(res?.infoList?.[0]?.tagName);
                          setCurrentItem(res?.infoList?.[0]?.itemList?.[0]);
                          showDrawer();
                        });
                      }
                    }
                  }
                }}
              >
                生成代码
              </Button>
              <Button
                type='default'
                size='middle'
                onClick={() => {
                  scanPackageFunc();
                  setModalVisible(true);
                }}
              >
                生成前端界面
              </Button>
            </Space>
          </span>

          <Form
            form={form}
            layout='vertical'
            initialValues={{
              ...initData,
            }}
          >
            <Row>
              <div className={'tag-line'}>
                <Space>
                  <Checkbox
                    checked={
                      initData.genController ||
                      initData.genControllerReq ||
                      initData.genDto ||
                      initData.genEntity ||
                      initData.genMapper ||
                      initData.genMapperXml ||
                      initData.genService ||
                      initData.genServiceImpl ||
                      initData.genMapStruct
                    }
                    onChange={e => {
                      if (e.target.checked === true) {
                        setInitData({
                          ...initData,
                          genController: true,
                          genControllerReq: true,
                          genDto: activeTab !== 'custom',
                          genEntity: activeTab !== 'custom',
                          genMapper: true,
                          genMapperXml: activeTab !== 'custom',
                          genService: true,
                          genServiceImpl: true,
                          genMapStruct: activeTab !== 'custom',
                        });
                        setUrlPrefixRequired(true);
                      } else {
                        clearAllCheckBox();
                        setUrlPrefixRequired(false);

                        form.resetFields(['urlPrefix']);
                      }
                    }}
                  ></Checkbox>
                  <span>生成类型（不勾选则无法生成）</span>
                </Space>
              </div>
            </Row>

            <Row gutter={[16, 8]}>
              <Col xs={12} md={4}>
                <Checkbox
                  disabled={activeTab === 'custom'}
                  checked={initData.genEntity}
                  onChange={e => handleUpdateGenerateType('genEntity', e.target.checked)}
                >
                  实体类 (Entity)
                </Checkbox>
              </Col>
              <Col xs={12} md={4}>
                <Checkbox
                  checked={initData.genController}
                  onChange={e => handleUpdateGenerateType('genController', e.target.checked)}
                >
                  Controller
                </Checkbox>
              </Col>

              <Col xs={12} md={4}>
                <Checkbox
                  checked={initData.genControllerReq}
                  onChange={e => handleUpdateGenerateType('genControllerReq', e.target.checked)}
                >
                  Controller.Req
                </Checkbox>
              </Col>
              <Col xs={12} md={4}>
                <Checkbox
                  disabled={activeTab === 'custom'}
                  checked={initData.genDto}
                  onChange={e => handleUpdateGenerateType('genDto', e.target.checked)}
                >
                  DTO
                </Checkbox>
              </Col>
              <Col xs={12} md={4}>
                <Checkbox
                  checked={initData.genMapper}
                  onChange={e => handleUpdateGenerateType('genMapper', e.target.checked)}
                >
                  Mapper
                </Checkbox>
              </Col>
              <Col xs={12} md={4}>
                <Checkbox
                  disabled={activeTab === 'custom'}
                  checked={initData.genMapperXml}
                  onChange={e => handleUpdateGenerateType('genMapperXml', e.target.checked)}
                >
                  MapperXML
                </Checkbox>
              </Col>
              <Col xs={12} md={4}>
                <Checkbox
                  checked={initData.genService}
                  onChange={e => handleUpdateGenerateType('genService', e.target.checked)}
                >
                  Service
                </Checkbox>
              </Col>
              <Col xs={12} md={4}>
                <Checkbox
                  checked={initData.genServiceImpl}
                  onChange={e => handleUpdateGenerateType('genServiceImpl', e.target.checked)}
                >
                  Service.Impl
                </Checkbox>
              </Col>
              <Col xs={12} md={4}>
                <Checkbox
                  disabled={activeTab === 'custom'}
                  checked={initData.genMapStruct}
                  onChange={e => handleUpdateGenerateType('genMapStruct', e.target.checked)}
                >
                  MapStruct&nbsp;
                  <QuestionCircleOutlined
                    title={'(增量写入)不受存在则删除影响，只要勾选了就会重新写入'}
                  />
                </Checkbox>
              </Col>
            </Row>

            <div style={{ display: activeTab === 'standard' ? 'block' : 'none' }}>
              <Row>
                <div className={'tag-line'}> 数据库配置</div>
              </Row>
              <Row gutter={[16, 8]}>
                <Col xs={24} md={6}>
                  <Form.Item name='url' label='jdbc url'>
                    <Input placeholder='jdbc:mysql://localhost:3306/test' />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name='username' label='用户名'>
                    <Input placeholder='数据库用户名' />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name='password' label='密码'>
                    <Input.Password placeholder='数据库密码' />
                  </Form.Item>
                </Col>
                <Col xs={24} md={6}>
                  <Form.Item name='tablePrefix' label='表前缀（以%结尾，和写sql一样）'>
                    <Input placeholder='要扫描的表前缀，例如：xxx_%' />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={[16, 8]}>
                <Col xs={24} md={6}>
                  <Form.Item name='removeTablePrefix' label='去除表格前缀'>
                    <Input placeholder='去除的表格前缀' />
                  </Form.Item>
                </Col>
                <Col xs={24} md={16}>
                  <Form.Item
                    name='exclude'
                    label={`排除表（该数据源共${initData?.allTables?.length ?? 0}张表）`}
                  >
                    <Select
                      mode='multiple'
                      placeholder='选择要排除的表'
                      style={{ width: '100%' }}
                      disabled={!displayTables}
                      labelInValue={true}
                    >
                      {displayTables &&
                        initData?.allTables?.map(e => {
                          return <Option key={e}>{e}</Option>;
                        })}
                    </Select>
                  </Form.Item>
                </Col>
                {/*懒得构造子组件去传递props 直接拉出来*/}
                <Col
                  xs={24}
                  md={2}
                  style={{
                    height: 'inherit',
                    display: 'flex',
                    paddingTop: '30px',
                    boxSizing: 'border-box',
                    justifyContent: 'center',
                  }}
                >
                  <Button
                    type={'primary'}
                    onClick={() => {
                      if (displayTables === false) {
                        setDisplayTables(true);
                      } else {
                        form.setFieldValue('exclude', []);
                        setDisplayTables(false);
                      }
                    }}
                  >
                    {displayTables ? '隐藏' : '显示'}表集合
                  </Button>
                </Col>
              </Row>
            </div>
            {/*custom*/}
            {activeTab === 'custom' && (
              <div>
                <Row>
                  <div className={'tag-line'}>
                    <span>
                      自定义属性&nbsp;&nbsp;【自定义生成之前需要提前从数据库中生成
                      Entity、Dto、MapperXml、MapStruct，不然代码会爆红】
                    </span>

                    <span>
                      <Button
                        type={'primary'}
                        size={'small'}
                        onClick={() => {
                          scanPackageFunc();
                        }}
                      >
                        重新扫描DTO和实体
                      </Button>
                    </span>
                  </div>
                </Row>
                <Row gutter={[16, 8]} wrap={true}>
                  <Col xs={24} md={6}>
                    <Form.Item
                      name='domainName'
                      label='实体名称'
                      rules={[{ required: true, message: '请输入实体名称' }]}
                    >
                      <Input placeholder='实体名称 驼峰 帕斯卡命名发 首字母必须是大写' />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item
                      name='cnDesc'
                      label='中文描述'
                      rules={[{ required: true, message: '请输入中文描述' }]}
                    >
                      <Input placeholder='请输入简短的中文描述' />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item
                      name='returnDtoName'
                      label='DTO的名称(自动扫描已有的)'
                      rules={[{ required: true, message: '请选择关联的DTO' }]}
                    >
                      {/*scanPackages*/}
                      <Select
                        //mode="multiple"
                        placeholder='选择DTO'
                        style={{ width: '100%' }}
                        showSearch
                        onSelect={e => {
                          if (e.replace) {
                            let replace = e.replace('Dto', '');
                            let find = scanPackages?.allEntitys?.find(e => e === replace);
                            if (find) {
                              form.setFieldValue('entityName', find);
                            }
                          }
                        }}
                      >
                        {scanPackages?.allDtos?.map(e => {
                          return <Option key={e}>{e}</Option>;
                        })}
                      </Select>
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item
                      name='entityName'
                      label='数据库实体名称(自动扫描已有的)'
                      rules={[{ required: true, message: '请选择关联的数据库实体' }]}
                    >
                      <Select
                        //mode="multiple"
                        placeholder='选择数据库实体'
                        style={{ width: '100%' }}
                        showSearch
                      >
                        {scanPackages?.allEntitys?.map(e => {
                          return <Option key={e}>{e}</Option>;
                        })}
                      </Select>
                    </Form.Item>
                  </Col>

                  {/* <Col xs={24} md={2} style={{
                                            height: 'inherit',
                                            display: 'flex',
                                            paddingTop: '30px',
                                            boxSizing: 'border-box',
                                            justifyContent: 'center',
                                        }}>
                                            <Button onClick={() => {
                                                scanPackageFunc();
                                            }}>重新扫描DTO和实体</Button>
                                        </Col>*/}
                </Row>
              </div>
            )}

            <Row>
              <div className={'tag-line'}>全局配置</div>
            </Row>

            <Row gutter={[16, 8]}>
              <Col xs={24} md={6}>
                <Form.Item
                  name='parentPackageName'
                  label='父包名称'
                  rules={[{ required: true, message: '请输入父包名称' }]}
                >
                  <Input placeholder='请输入父包名称，如 com.example' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='projectAbsolutePath'
                  label='项目所在绝对路径'
                  rules={[{ required: true, message: '请输入项目所在绝对路径' }]}
                >
                  <Input placeholder='请输入项目绝对路径' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='urlPrefix'
                  label='生成controller中的url前缀'
                  rules={[
                    { required: urlPrefixRequired, message: '请输入生成controller中的url前缀' },
                  ]}
                >
                  <Input placeholder='格式为 xxx/xxx，后面的地址自动生成' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item name='headerDesc' label='类文件头注释'>
                  <Input placeholder='类文件头注释' />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={[16, 8]}>
              <Col xs={24} md={6}>
                <Form.Item name='author' label='作者'>
                  <Input placeholder='作者名称' />
                </Form.Item>
              </Col>
              <Col
                xs={24}
                md={6}
                style={{
                  height: 'inherit',
                  display: 'flex',
                  paddingTop: '22px',
                  boxSizing: 'border-box',
                  // justifyContent: 'center',
                }}
              >
                <div style={{ marginTop: 8 }}>
                  <Form.Item name='deleteIfExists' valuePropName='checked' noStyle>
                    <Checkbox>存在则删除</Checkbox>
                  </Form.Item>
                </div>
              </Col>
            </Row>

            <Row gutter={[16, 8]}>
              <Col xs={24} md={6}>
                <Form.Item
                  name='entityPackageName'
                  label='domains 路径'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='domains 路径' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='controllerPackageName'
                  label='controller 路径'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='controller 路径' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='controllerReqPackageName'
                  label='controller.req 路径'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='controller.req 路径' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='dtoPackageName'
                  label='dto 路径'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='dto 路径' />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={[16, 8]}>
              <Col xs={24} md={6}>
                <Form.Item
                  name='mapperPackageName'
                  label='mapper 路径'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='mapper 路径' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='mapperXmlPackageName'
                  label='xml路径'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='xml路径' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='serviceInterfacePackageName'
                  label='service 路径'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='service 路径' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='serviceImplPackageName'
                  label='service.impl 路径'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='service.impl 路径' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='mapperStructPackageName'
                  label='MapStruct 路径'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='MapStruct 路径' />
                </Form.Item>
              </Col>
              <Col xs={24} md={6}>
                <Form.Item
                  name='mapperStructClassSimpleName'
                  label='MapStruct类名'
                  rules={[{ required: true, message: '请输入路径' }]}
                >
                  <Input placeholder='MapStruct 路径，不要以.java结尾' />
                </Form.Item>
              </Col>

             
            </Row>
            
            <Row gutter={[16, 8]}>
               <Col xs={24} md={24}>
                <Form.Item
                  name='createTimeName'
                  label='创建时间字段名称'
                  rules={[
                    { required: true, message: '请输入创建时间字段名称' }
                  ]}
                >
                  <Input placeholder='创建时间字段名称' />
                </Form.Item>
              </Col>

              <Col xs={24} md={6}>
                <Form.Item
                  name='isEnabledName'
                  label='是否启用字段名称'
                  rules={[
                    { required: true, message: '请输入是否启用字段名称' }
                  ]}
                >
                  <Input placeholder='是否启用字段名称' />
                </Form.Item>
              </Col>

              <Col xs={24} md={6}>
                <Form.Item
                  name='isEnabledValid'
                  label='已启用对应的值'
                  rules={[{ required: true, message: '请输入已启用对应的值' }]}
                >
                  <Input placeholder='已启用对应的值' />
                </Form.Item>
              </Col>

              <Col xs={24} md={6}>
                <Form.Item
                  name='isEnabledNotValid'
                  label='未启用对应的值'
                  rules={[{ required: true, message: '请输入未启用对应的值' }]}
                >
                  <Input placeholder='未启用对应的值' />  
                </Form.Item>
              </Col>

              <Col xs={24} md={6}>
                <Form.Item
                  name='isEnabledIsNumber'
                  label='是否启用字段是否为数字'
                  rules={[{ required: true, message: '请输入是否启用字段是否为数字' }]}
                  valuePropName='checked'
                  initialValue={true}
                >
                  <Switch />  
                </Form.Item>
              </Col>
              

              <Col xs={24} md={6}>
                <Form.Item
                  name='isDeletedName'
                  label='是否删除字段名称'
                  rules={[
                    { required: true, message: '请输入是否删除字段名称' }
                  ]}
                >
                  <Input placeholder='是否删除字段名称' />
                </Form.Item>
              </Col>

              <Col xs={24} md={6}>
                <Form.Item
                  name='isDeletedValid'
                  label='已删除对应的值'
                  rules={[{ required: true, message: '请输入已删除对应的值' }]}
                >
                  <Input placeholder='已删除对应的值' />
                </Form.Item>
              </Col>

              <Col xs={24} md={6}>
                <Form.Item
                  name='isDeletedNotValid'
                  label='未删除对应的值'
                  rules={[{ required: true, message: '请输入未删除对应的值' }]}
                >
                  <Input placeholder='未删除对应的值' />
                </Form.Item>
              </Col>

               <Col xs={24} md={6}>
                <Form.Item
                  name='isDeletedIsNumber'
                  label='是否删除字段是否为数字'
                  rules={[{ required: true, message: '请输入是否删除字段是否为数字' }]}
                  valuePropName='checked'
                  initialValue={true}
                >
                  <Switch />  
                </Form.Item>
              </Col>
            </Row>
          </Form>
        </div>
      </Content>

      <Footer className='app-footer'>© 2025 Easy4j底座代码生成工具</Footer>

      <Modal
        title='模型选择'
        open={modalVisible}
        onCancel={() => {
          modalForm.resetFields();
          setModalVisible(false);
          modalForm.resetFields();
        }}
        width={"50%"}
        centered
        onOk={async () => {
          let values = await modalForm.validateFields();
          frontPageInit(values.dtoName, values.domainName, values.controllerName);
        }}
        okText='确认'
        cancelText='取消'
      >
        <Form form={modalForm} layout='vertical'>
          <Row gutter={[16, 8]}>
            <Col xs={24} md={8}>
              <Form.Item
                name='dtoName'
                label='前端字段集'
                rules={[{ required: true, message: '请输入前端字段集' }]}
              >
                <Select
                  placeholder='选择前端字段集'
                  style={{ width: '100%' }}
                  showSearch
                  onSelect={e => {
                    let replace = e.replace('Dto', '');
                    let find = scanPackages?.allEntitys?.find(e2 => e2 === replace);
                    let findController = scanPackages?.allControllers?.find(e2 => e2.replace('Controller','') === replace);
                    if (find) {
                      modalForm.setFieldValue('domainName', find);
                    }
                    if(findController){
                        modalForm.setFieldValue('controllerName', findController);
                    }
                  }}
                >
                  {scanPackages?.allDtos?.map(e => {
                    return <Option key={e}>{e}</Option>;
                  })}
                </Select>
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item
                name='domainName'
                label='数据库字段集'
                rules={[{ required: true, message: '请选择数据库字段集' }]}
              >
                <Select placeholder='选择数据库字段集' style={{ width: '100%' }} showSearch>    
                  {scanPackages?.allEntitys?.map(e => {
                    return <Option key={e}>{e}</Option>;
                  })}
                </Select>
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item
                name='controllerName'
                label='控制器'
                rules={[{ required: true, message: '请选择控制器' }]}
              >
                <Select placeholder='选择控制器' style={{ width: '100%' }} showSearch>    
                  {scanPackages?.allControllers?.map(e => {
                    return <Option key={e}>{e}</Option>;
                  })}
                </Select>
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      <Drawer
        title='预览'
        closable={{ 'aria-label': 'Close Button' }}
        onClose={onClose}
        width={'80%'}
        open={open}
      >
        <Tabs
          activeKey={activeTab2}
          onChange={e => {
            setActiveTab2(e);
            let filterElement = previewText?.infoList?.filter(e2 => e2.tagName === e)?.[0]
              ?.itemList?.[0];
            setCurrentItem(filterElement);
          }}
          tabPlacement={'top'}
          tabBarStyle={{ backgroundColor: 'white' }}
        >
          {previewText?.infoList?.map((item, index) => {
            return (
              <TabPane tab={item.tagName} key={item.tagName} children={null}>
                <div
                  style={{
                    display: 'flex',
                    gap: 15,
                  }}
                >
                  <List
                    bordered={false}
                    dataSource={item.itemList}
                    renderItem={item => (
                      <List.Item
                        style={{
                          color: currentItem?.fileName === item.fileName ? '#16A4FF' : '#000',
                          width: '200px',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                          textWrap: 'nowrap',
                        }}
                        onClick={() => {
                          setCurrentItem(item);
                        }}
                        title={item.fileName}
                      >
                        <Space>
                          <CopyOutlined
                            onClick={async () => {
                              // 核心：复制文本到剪贴板
                              await navigator.clipboard.writeText(item.preview);
                              message.info('复制成功！');
                            }}
                          />
                          {item.fileName}
                        </Space>
                      </List.Item>
                    )}
                  />
                  <div style={{ width: '100%' }}>
                    <SyntaxHighlighter
                      language='java' // 指定语言（支持 js/ts/html/css/python 等）
                      style={vs} // 主题样式
                      showLineNumbers={true} // 显示行号
                      lineNumberStyle={{ color: '#999', fontSize: 12 }} // 行号样式
                    >
                      {currentItem?.preview}
                    </SyntaxHighlighter>
                  </div>
                </div>
              </TabPane>
            );
          })}
        </Tabs>
      </Drawer>
      <Vue3ArcoSupertable
        pageInitData={pageInitData}
        open={superTableVisible}
        onClose={() => setSuperTableVisible(false)}
      />
    </Layout>
  );
}

export default App;
