package easy4j.module.mybatisplus.codegen;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.module.mybatisplus.codegen.controller.ControllerGen;
import easy4j.module.mybatisplus.codegen.controller.ControllerReqGen;
import easy4j.module.mybatisplus.codegen.db.DbGenSetting;
import easy4j.module.mybatisplus.codegen.db.DbGen;
import easy4j.module.mybatisplus.codegen.mybatis.MapperGen;
import easy4j.module.mybatisplus.codegen.service.IServiceGen;
import easy4j.module.mybatisplus.codegen.service.ServiceImplGen;
import easy4j.module.mybatisplus.codegen.servlet.PreviewRes;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

public class AutoGen {

    public List<CodeGen> genList = new ArrayList<>();

    GenDto[] genDto;


    private AutoGen(GenDto... genDto) {
        this.genDto = genDto;
    }

    public static AutoGen build(GenDto... genDto) {
        return new AutoGen(genDto);
    }

    public AutoGen genController() {
        for (GenDto dto : genDto) {
            ControllerGen controllerGen1 = new ControllerGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            genList.add(controllerGen1);
        }

        return this;
    }

    public AutoGen genController(boolean deleteIfExists) {
        for (GenDto dto : genDto) {
            ControllerGen controllerGen1 = new ControllerGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            controllerGen1.setDeleteIfExists(deleteIfExists);
            genList.add(controllerGen1);
        }
        return this;
    }

    public AutoGen genControllerReq() {
        for (GenDto dto : genDto) {
            ControllerReqGen controllerGen1 = new ControllerReqGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            genList.add(controllerGen1);
        }
        return this;
    }

    public AutoGen genControllerReq(boolean deleteIfExists) {
        for (GenDto dto : genDto) {
            ControllerReqGen controllerGen1 = new ControllerReqGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            controllerGen1.setDeleteIfExists(deleteIfExists);
            genList.add(controllerGen1);
        }
        return this;
    }

    public AutoGen genIService(boolean deleteIfExists) {
        for (GenDto dto : genDto) {
            IServiceGen controllerGen1 = new IServiceGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            controllerGen1.setDeleteIfExists(deleteIfExists);
            genList.add(controllerGen1);
        }
        return this;
    }

    public AutoGen genIService() {
        for (GenDto dto : genDto) {
            IServiceGen controllerGen1 = new IServiceGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            genList.add(controllerGen1);
        }
        return this;
    }

    public AutoGen genServiceImpl() {
        for (GenDto dto : genDto) {
            ServiceImplGen controllerGen1 = new ServiceImplGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            genList.add(controllerGen1);
        }
        return this;
    }

    public AutoGen genServiceImpl(boolean deleteIfExists) {
        for (GenDto dto : genDto) {
            ServiceImplGen controllerGen1 = new ServiceImplGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            controllerGen1.setDeleteIfExists(deleteIfExists);
            genList.add(controllerGen1);
        }
        return this;
    }

    public AutoGen fromDbGen(DbGenSetting dbGenSetting) {
        GenDto genDto1 = ListTs.get(genDto, 0);
        if (genDto1 == null) {
            throw new IllegalArgumentException("请在build方法中传入配置");
        }
        DbGen controllerGen1 = new DbGen(dbGenSetting);
        BeanUtil.copyProperties(genDto1, controllerGen1);
        genList.add(controllerGen1);
        return this;
    }

    public AutoGen fromDbGen(DbGenSetting dbGenSetting, boolean deleteIfExists) {
        GenDto genDto1 = ListTs.get(genDto, 0);
        if (genDto1 == null) {
            throw new IllegalArgumentException("请在build方法中传入配置");
        }
        DbGen controllerGen1 = new DbGen(dbGenSetting);
        BeanUtil.copyProperties(genDto1, controllerGen1);
        controllerGen1.setDeleteIfExists(deleteIfExists);
        genList.add(controllerGen1);
        return this;
    }

    public AutoGen genMapper() {
        for (GenDto dto : genDto) {
            MapperGen controllerGen1 = new MapperGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            genList.add(controllerGen1);
        }
        return this;
    }

    public AutoGen genMapper(boolean deleteIfExists) {
        for (GenDto dto : genDto) {
            MapperGen controllerGen1 = new MapperGen();
            BeanUtil.copyProperties(dto, controllerGen1);
            controllerGen1.setDeleteIfExists(deleteIfExists);
            genList.add(controllerGen1);
        }
        return this;
    }


    public void gen() {
        for (CodeGen codeGen : genList) {
            String gen = codeGen.gen(false, false, new ObjectValue());
            if (StrUtil.isNotBlank(gen)) System.out.println(gen);
        }
    }

    public PreviewRes custom(boolean isPreview, boolean isServer) {
        PreviewRes res = new PreviewRes();
        for (CodeGen codeGen : genList) {
            ObjectValue objectValue = new ObjectValue();
            String gen = codeGen.gen(isPreview, isServer, objectValue);
            Object object = objectValue.getObject();
            if (null != object) {
                PreviewRes object1 = (PreviewRes) object;
                ListTs.addAll(res.getInfoList(), object1.getInfoList());
            }
            if (StrUtil.isNotBlank(gen)) System.out.println(gen);
        }
        return res;
    }

    public String preview() {
        List<String> lineList = ListTs.newList();
        for (CodeGen codeGen : genList) {
            String gen = codeGen.gen(true, false, new ObjectValue());
            if (StrUtil.isNotBlank(gen)) lineList.add(gen);
        }
        return String.join("\n", lineList);
    }

    public void gen(boolean isServer) {
        for (CodeGen codeGen : genList) {
            String gen = codeGen.gen(false, isServer, new ObjectValue());
            if (StrUtil.isNotBlank(gen)) System.out.println(gen);
        }
    }

    public String preview(boolean isServer) {
        List<String> lineList = ListTs.newList();
        for (CodeGen codeGen : genList) {
            String gen = codeGen.gen(true, isServer, new ObjectValue());
            if (StrUtil.isNotBlank(gen)) lineList.add(gen);
        }
        return String.join("\n", lineList);
    }

    public PreviewRes auto(boolean isPreview, boolean isServer) {
        ObjectValue objectValue = new ObjectValue();
        for (CodeGen codeGen : genList) {
            if (codeGen instanceof DbGen) {
                codeGen.gen(isPreview, isServer, objectValue);
                break;
            }
        }
        return (PreviewRes) objectValue.getObject();
    }

    public AutoGen clearAllExistsFiles() {
        for (CodeGen codeGen : genList) {
            codeGen.clear();
        }
        return this;
    }

    public static void main(String[] args) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        System.out.println("pid " + Integer.parseInt(runtimeMXBean.getName().split("@")[0]));
        GenDto genDto1 = new GenDto()
                .setAuthor("bokun")
                .setParentPackageName("com.ssc.dataspace.approval2")
                .setProjectAbsolutePath("E:\\IdeaProjects\\ssc\\dataspace-approval-service")
                .setUrlPrefix("ssc/flow")
                .setDeleteIfExists(true)
                //-------------------------------
                .setCnDesc("动态表单")
                .setDomainName("FlowFormSetting")
                .setParentPackageName("com.ssc.dataspace.approval")
                .setReturnDtoName("FlowFormSettingDto")
                .setProjectAbsolutePath("E:\\IdeaProjects\\ssc\\dataspace-approval-service");

        AutoGen.build(MultiGenDto.build(
                                new GlobalGenConfig()
                                        .setAuthor("bokun")
                                        .setParentPackageName("com.ssc.dataspace.approval2")
                                        .setProjectAbsolutePath("E:\\IdeaProjects\\ssc\\dataspace-approval-service")
                                        .setUrlPrefix("ssc/flow")
                                        .setDeleteIfExists(true)
                                        .setMapperXmlPackageName("mappers2")
                        )
                        .multiGen(
                                "FormSetting-FlowFormSettingDto-动态表单-FlowFormSetting",
                                "FormValue-FlowFormContentDto-动态表单值-FlowFormContent",
                                "SpConfirm-FlowSpConfirmDto-审批操作配置-FlowSpConfirm",
                                "Def-FlowProcDefDto-审批流程定义-FlowProcDef",
                                "Instance-FlowProcInstDto-审批流程实例-FlowProcInst",
                                "Copy-FlowSpCopyDto-审批流程抄送-FlowSpCopy",
                                "SpContent-FlowSpContentDto-审批内容配置-FlowSpContent"
                        ))
                .fromDbGen(new DbGenSetting()
                        .setUrl("jdbc:postgresql://10.0.32.19:30163/ds")
                        .setUsername("drhi_user")
                        .setPassword("drhi_password")
                        .setTablePrefix("ssc_flow_%")
                        .setRemoveTablePrefix("ssc_")
                        .setGenEntity(true)
                        .setGenMapperXml(true)
                        .setGenMapper(true)
                        .setGenDto(true)
                        .setGenService(true)
                        .setGenServiceImpl(true)
                        .setGenController(true)
                        .setGenControllerReq(true)
                )
//                .genMapper() // 外围的这几个是配合multiGen使用的
//                .genController()
//                .genControllerReq()
//                .genIService()
//                .genServiceImpl()
//                .clearAllExistsFiles()
                .gen();
    }


}
