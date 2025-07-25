package template.service.api.client;


import easy4j.infra.common.header.EasyResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import template.service.api.dto.AdviceStorageDto;

@FeignClient(name = "template-storage")
public interface TemplateStorageApi {

    @PutMapping("/advice-storage")
    EasyResult<Object> updateStorage(@RequestBody AdviceStorageDto storage);

    @GetMapping("/advice-storage/{ordCode}")
    EasyResult<AdviceStorageDto> getStorage(@PathVariable String ordCode);


    @PutMapping("/advice-storage/advice-tccFrozeStorage")
    EasyResult<AdviceStorageDto> tccFrozeStorage(@RequestBody AdviceStorageDto storage);

    @PutMapping("/advice-storage/advice-tccReduceStorage")
    EasyResult<AdviceStorageDto> tccReduceStorage(@RequestBody AdviceStorageDto storage);

    @PutMapping("/advice-storage/advice-tccCancelStorage")
    EasyResult<AdviceStorageDto> tccCancelStorage(@RequestBody AdviceStorageDto storage);
}
