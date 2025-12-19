package easy4j.infra.context.codegen;

import cn.hutool.core.bean.BeanUtil;

import java.util.ArrayList;
import java.util.List;

public class AutoGen {

    List<CodeGen> genList = new ArrayList<>();

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
            controllerGen1.setDeleteIfExists(deleteIfExists);
            BeanUtil.copyProperties(dto, controllerGen1);
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
            controllerGen1.setDeleteIfExists(deleteIfExists);
            BeanUtil.copyProperties(dto, controllerGen1);
            genList.add(controllerGen1);
        }
        return this;
    }

    public AutoGen genIService(boolean deleteIfExists) {
        for (GenDto dto : genDto) {
            IServiceGen controllerGen1 = new IServiceGen();
            controllerGen1.setDeleteIfExists(deleteIfExists);
            BeanUtil.copyProperties(dto, controllerGen1);
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
            controllerGen1.setDeleteIfExists(deleteIfExists);
            BeanUtil.copyProperties(dto, controllerGen1);
            genList.add(controllerGen1);
        }
        return this;
    }
    public void gen() {
        for (CodeGen codeGen : genList) {
            String gen = codeGen.gen();
            System.out.println(gen);
        }
    }

    public AutoGen clearAllExistsFiles(){
        for (CodeGen codeGen : genList) {
            codeGen.clear();
        }
        return this;
    }

    public static void main(String[] args) {

        GenDto genDto1 = new GenDto()
                .setCnDesc("动态表单")
                .setDomainName("FlowFormSetting")
                .setParentPackageName("com.ssc.dataspace.approval")
                .setReturnDtoName("FlowFormSettingDto")
                .setProjectAbsolutePath("E:\\IdeaProjects\\ssc\\dataspace-approval-service");

        GenDto[] genDtos = MultiGenDto.build()
                .setAuthor("bokun")
                .setParentPackageName("com.ssc.dataspace.approval")
                .setProjectAbsolutePath("E:\\IdeaProjects\\ssc\\dataspace-approval-service")
                .setUrlPrefix("ssc/flow")
                .setDeleteIfExists(true)
                .multiGen(
                        "FormSetting-FlowFormSettingDto-动态表单-FlowFormSetting",
                        "FormValue-FlowFormContentDto-动态表单值-FlowFormContent",
                        "SpConfirm-FlowSpConfirmDto-审批操作配置-FlowSpConfirm",
                        "Def-FlowProcDefDto-审批流程定义-FlowProcDef",
                        "Instance-FlowProcInstDto-审批流程实例-FlowProcInst",
                        "Copy-FlowSpCopyDto-审批流程抄送-FlowSpCopy",
                        "SpContent-FlowSpContentDto-审批内容配置-FlowSpContent"
                );
        AutoGen.build(genDtos)
                .genController()
                .genControllerReq()
                .genIService()
                .genServiceImpl()
//                .clearAllExistsFiles()
                .gen();
    }


}
