import {useState, useEffect, useRef} from 'react'
import {
    Layout, Card, Form, Input, Checkbox, Button, Select, Tabs,
    Table, Space, Typography, Divider, Row, Col, Drawer
} from 'antd'
import {PlusOutlined, DeleteOutlined, CopyOutlined, ClearOutlined} from '@ant-design/icons'
import './App.css'
import {post} from "./request.js";
import {useForm} from "antd/es/form/Form.js";

const {Header, Content, Footer} = Layout
const {Title, Text} = Typography
const {Option} = Select
const {TabPane} = Tabs

function App() {
    // 表单实例
    const [form] = Form.useForm()

    // 选项卡状态
    const [activeTab, setActiveTab] = useState('standard')

    // 生成类型状态（与包路径配置对应）
    const [generateTypes, setGenerateTypes] = useState({
        entity: true,
        controller: true,
        controllerReq: true,
        dto: true,
        mapper: true,
        service: true,
        serviceImpl: true
    })

    const [initData, setInitData] = useState({
        url: "",
        username: "",
        password: "",
        tablePrefix: "",
        exclude: [
            ""
        ],
        removeTablePrefix: "",
        parentPackageName: "",
        projectAbsolutePath: "",
        urlPrefix: "",
        deleteIfExists: false,
        headerDesc: "",
        author: "",
        forceDelete: false,
        entityPackageName: "",
        controllerPackageName: "",
        controllerReqPackageName: "",
        dtoPackageName: "",
        mapperPackageName: "",
        mapperXmlPackageName: "",
        serviceInterfacePackageName: "",
        serviceImplPackageName: "",
        allTables: [
            ""
        ],
        genController: false,
        genControllerReq: false,
        genDto: false,
        genEntity: false,
        genMapper: false,
        genMapperXml: false,
        genService: false,
        genServiceImpl: false
    })

    const [previewText, setPreviewText] = useState("")

    const [open, setOpen] = useState(false);
    const showDrawer = () => {
        setOpen(true);
    };
    const onClose = () => {
        setOpen(false);
    };
    useEffect(() => {
        post("/init", {}).then(res => {
            setInitData(res)
            form.setFieldsValue(res); // 动态赋值
        })
        // 可选：清理逻辑（按需添加）
        return () => {
            // 清理代码
        };
    }, [])

    // 输出状态
    const [outputCode, setOutputCode] = useState('// 生成的代码将显示在这里...')
    const [isGenerating, setIsGenerating] = useState(false)

    // 更新生成类型
    const handleUpdateGenerateType = (type, value) => {
        if(type === 'genController' || type === 'genControllerReq'){
            setInitData(prev => ({...initData, genController: value,genControllerReq:value}))
        }else{
            setInitData(prev => ({...initData, [type]: value}))
        }


    }

    // 生成Java代码（模拟接口调用）
    const handleGenerate = async () => {
        setIsGenerating(true)

        try {
            // 获取表单值
            const formValues = await form.validateFields()

            // 模拟API请求生成代码
            await new Promise(resolve => setTimeout(resolve, 1000))

        } catch (error) {
            console.error('生成代码失败:', error)
            setOutputCode('// 生成代码失败，请检查配置后重试')
        } finally {
            setIsGenerating(false)
        }
    }
    return (
        <Layout className="app-layout">
            <Header className="app-header">
                <Title level={2} className="header-title">Easy4j底座代码生成工具</Title>
            </Header>

            <Content className="app-content">
                <Tabs activeKey={activeTab} onChange={setActiveTab} tabPlacement={"start"}
                      tabBarStyle={{backgroundColor: "white"}}>
                    <TabPane tab="从数据库中生成" key="standard" children={null}>
                    </TabPane>
                    <TabPane tab="自定义生成" key="custom" children={null}>
                    </TabPane>
                </Tabs>

                <div className="standard-form" style={{display: activeTab === "standard" ? "block" : "none"}}>
                    <span>
                        <Space>
                             <Button
                                 type="primary"
                                 size="large"
                                 loading={isGenerating}
                                 onClick={async e => {
                                     const formValues = await form.validateFields()
                                     let parse = JSON.parse(JSON.stringify({
                                         ...initData,
                                         ...formValues
                                     }));
                                     delete parse.allTables;
                                     delete parse.exclude;
                                     post("/db/gen", parse,{loadingText:"正在生成，生成之后会写入文件"}).then(res => {
                                         setPreviewText(res);
                                         showDrawer();
                                     })
                                 }}
                             >
                              生成代码
                          </Button>
                            <Button
                                type="primary"
                                size="large"
                                onClick={async e => {
                                    const formValues = await form.validateFields()
                                    let parse = JSON.parse(JSON.stringify({
                                        ...initData,
                                        ...formValues
                                    }));
                                    delete parse.allTables;
                                    delete parse.exclude;
                                    post("/db/preview", parse,{loadingText:"正在生成预览"}).then(res => {
                                        setPreviewText(res);
                                        showDrawer();
                                    })
                                }}
                            >
                          预览代码
                      </Button>
                        </Space>

                      </span>

                    <Form
                        form={form}
                        layout="vertical"
                        initialValues={{
                            ...initData
                        }}
                    >
                        <Row>
                            <div className={"tag-line"}>
                                <Space>
                                    <Checkbox
                                        checked={initData.genEntity}
                                        onChange={(e) => {
                                            if (e.target.checked === true) {
                                                setInitData({
                                                    genController: true,
                                                    genControllerReq: true,
                                                    genDto: true,
                                                    genEntity: true,
                                                    genMapper: true,
                                                    genMapperXml: true,
                                                    genService: true,
                                                    genServiceImpl: true
                                                })
                                            }else {
                                                setInitData({
                                                    genController: false,
                                                    genControllerReq: false,
                                                    genDto: false,
                                                    genEntity: false,
                                                    genMapper: false,
                                                    genMapperXml: false,
                                                    genService: false,
                                                    genServiceImpl: false
                                                })
                                            }

                                        }}
                                    >
                                    </Checkbox>
                                    <span>生成类型</span>
                                </Space>

                            </div>
                        </Row>

                        <Row gutter={[16, 8]}>
                            <Col xs={12} md={6}>
                                <Checkbox
                                    checked={initData.genEntity}
                                    onChange={(e) => handleUpdateGenerateType('genEntity', e.target.checked)}
                                >
                                    实体类 (Entity)
                                </Checkbox>
                            </Col>
                            <Col xs={12} md={6}>
                                <Checkbox
                                    checked={initData.genController}
                                    onChange={(e) => handleUpdateGenerateType('genController', e.target.checked)}
                                >
                                    Controller
                                </Checkbox>
                            </Col>

                            <Col xs={12} md={6}>
                                <Checkbox
                                    checked={initData.genControllerReq}
                                    onChange={(e) => handleUpdateGenerateType('genControllerReq', e.target.checked)}
                                >
                                    Controller.Req
                                </Checkbox>
                            </Col>
                            <Col xs={12} md={6}>
                                <Checkbox
                                    checked={initData.genDto}
                                    onChange={(e) => handleUpdateGenerateType('genDto', e.target.checked)}
                                >
                                    DTO
                                </Checkbox>
                            </Col>
                            <Col xs={12} md={6}>
                                <Checkbox
                                    checked={initData.genMapper}
                                    onChange={(e) => handleUpdateGenerateType('genMapper', e.target.checked)}
                                >
                                    Mapper
                                </Checkbox>
                            </Col>
                            <Col xs={12} md={6}>
                                <Checkbox
                                    checked={initData.genMapperXml}
                                    onChange={(e) => handleUpdateGenerateType('genMapperXml', e.target.checked)}
                                >
                                    MapperXML
                                </Checkbox>
                            </Col>
                            <Col xs={12} md={6}>
                                <Checkbox
                                    checked={initData.genService}
                                    onChange={(e) => handleUpdateGenerateType('genService', e.target.checked)}
                                >
                                    Service
                                </Checkbox>
                            </Col>
                            <Col xs={12} md={6}>
                                <Checkbox
                                    checked={initData.genServiceImpl}
                                    onChange={(e) => handleUpdateGenerateType('genServiceImpl', e.target.checked)}
                                >
                                    Service.Impl
                                </Checkbox>
                            </Col>
                        </Row>

                        <Row>
                            <div className={"tag-line"}> 数据库配置</div>
                        </Row>
                        <Row gutter={[16, 8]}>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="url"
                                    label="jdbc url"
                                >
                                    <Input placeholder="jdbc:mysql://localhost:3306/test"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="username"
                                    label="用户名"
                                >
                                    <Input placeholder="数据库用户名"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="password"
                                    label="密码"
                                >
                                    <Input.Password placeholder="数据库密码"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="tablePrefix"
                                    label="表前缀"
                                >
                                    <Input placeholder="要扫描的表前缀，例如：xxx_%"/>
                                </Form.Item>
                            </Col>
                        </Row>

                        <Row gutter={[16, 8]}>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="removeTablePrefix"
                                    label="去除表格前缀"
                                >
                                    <Input placeholder="去除的表格前缀"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={18}>
                                <Form.Item
                                    name="exclude"
                                    label="排除表"
                                >
                                    <Select
                                        mode="multiple"
                                        placeholder="选择要排除的表"
                                        style={{width: '100%'}}
                                    >
                                        {
                                            initData?.allTables?.map(e => {
                                                return <Option key={e}>{e}</Option>
                                            })
                                        }
                                    </Select>
                                </Form.Item>
                            </Col>
                        </Row>


                        <Row>
                            <div className={"tag-line"}>全局配置</div>
                        </Row>

                        <Row gutter={[16, 8]}>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="parentPackageName"
                                    label="父包名称"
                                    rules={[{required: true, message: '请输入父包名称'}]}
                                >
                                    <Input placeholder="请输入父包名称，如 com.example"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="projectAbsolutePath"
                                    label="项目所在绝对路径"
                                >
                                    <Input placeholder="请输入项目绝对路径"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="urlPrefix"
                                    label="生成controller中的url前缀"
                                >
                                    <Input placeholder="格式为 xxx/xxx，后面的地址自动生成"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="headerDesc"
                                    label="类文件头注释"
                                >
                                    <Input placeholder="类文件头注释"/>
                                </Form.Item>
                            </Col>
                        </Row>

                        <Row gutter={[16, 8]}>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="author"
                                    label="作者"
                                >
                                    <Input placeholder="作者名称"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <div style={{marginTop: 8}}>
                                    <Form.Item name="deleteIfExists" valuePropName="checked" noStyle>
                                        <Checkbox>存在则删除</Checkbox>
                                    </Form.Item>
                                </div>
                            </Col>
                        </Row>

                        <Row gutter={[16, 8]}>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="entityPackageName"
                                    label="domains 路径"
                                >
                                    <Input placeholder="domains 路径"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="controllerPackageName"
                                    label="controller 路径"
                                >
                                    <Input placeholder="controller 路径"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="controllerReqPackageName"
                                    label="controller.req 路径"
                                >
                                    <Input placeholder="controller.req 路径"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="dtoPackageName"
                                    label="dto 路径"
                                >
                                    <Input placeholder="dto 路径"/>
                                </Form.Item>
                            </Col>
                        </Row>

                        <Row gutter={[16, 8]}>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="mapperPackageName"
                                    label="mapper 路径"
                                >
                                    <Input placeholder="mapper 路径"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="mapperXmlPackageName"
                                    label="xml路径"
                                >
                                    <Input placeholder="xml路径"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="serviceInterfacePackageName"
                                    label="service 路径"
                                >
                                    <Input placeholder="service 路径"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={6}>
                                <Form.Item
                                    name="serviceImplPackageName"
                                    label="service.impl 路径"
                                >
                                    <Input placeholder="service.impl 路径"/>
                                </Form.Item>
                            </Col>
                        </Row>


                    </Form>
                </div>

            </Content>

            <Footer className="app-footer">
                <Text>© 2025 Easy4j底座代码生成工具</Text>
            </Footer>

            <Drawer
                title="预览"
                closable={{'aria-label': 'Close Button'}}
                onClose={onClose}
                width={"60%"}
                open={open}
            >
                <pre>
                {previewText}
                </pre>
            </Drawer>
        </Layout>
    )
}

export default App
