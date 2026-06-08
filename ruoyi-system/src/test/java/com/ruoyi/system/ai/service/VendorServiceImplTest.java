package com.ruoyi.system.ai.service;

import com.ruoyi.system.ai.crypto.LlmVendorCryptoService;
import com.ruoyi.system.ai.domain.AiVendor;
import com.ruoyi.system.ai.domain.bo.AiVendorBo;
import com.ruoyi.system.ai.mapper.AiVendorMapper;
import com.ruoyi.system.ai.service.impl.VendorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorServiceImplTest {

    @Mock AiVendorMapper mapper;
    @Mock LlmVendorCryptoService crypto;
    private VendorServiceImpl service;

    private VendorServiceImpl newService() {
        return new VendorServiceImpl(mapper, crypto);
    }

    @Test
    void insert_rejectsDuplicateName() {
        service = newService();
        when(mapper.countByName("DeepSeek")).thenReturn(1);
        AiVendorBo bo = new AiVendorBo();
        bo.setVendorName("DeepSeek");
        bo.setApiKey("sk-x");
        assertThrows(IllegalArgumentException.class, () -> service.insert(bo));
    }

    @Test
    void setDefault_rejectsDisabledVendor() {
        service = newService();
        AiVendor e = new AiVendor();
        e.setStatus("1");
        when(mapper.selectById(1L)).thenReturn(e);
        assertThrows(IllegalStateException.class, () -> service.setDefault(1L));
    }

    @Test
    void update_rejectsStoppingLastDefault() {
        service = newService();
        AiVendor e = new AiVendor();
        e.setIsDefault("1");
        e.setStatus("0");
        when(mapper.selectById(1L)).thenReturn(e);
        AiVendorBo bo = new AiVendorBo();
        bo.setVendorId(1L);
        bo.setStatus("1");
        assertThrows(IllegalStateException.class, () -> service.update(bo));
    }

    @Test
    void deleteByIds_rejectsRemovingDefault() {
        service = newService();
        AiVendor e = new AiVendor();
        e.setIsDefault("1");
        when(mapper.selectById(1L)).thenReturn(e);
        assertThrows(IllegalStateException.class, () -> service.deleteByIds(new Long[]{1L}));
    }
}
