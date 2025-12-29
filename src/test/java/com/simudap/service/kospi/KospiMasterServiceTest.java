package com.simudap.service.kospi;

import com.simudap.dto.stock.StockBaseInfo;
import com.simudap.model.kospi.KospiMaster;
import com.simudap.repository.kospi.KospiMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KospiMasterServiceTest {

    @Mock
    private KospiMasterRepository kospiMasterRepository;

    @InjectMocks
    private KospiMasterService kospiMasterService;

    private KospiMaster samsungStock;
    private KospiMaster skHynixStock;

    @BeforeEach
    void setUp() {
        samsungStock = new KospiMaster(
                "005930",
                "KR7005930003",
                "삼성전자",
                false
        );

        skHynixStock = new KospiMaster(
                "000660",
                "KR7000660001",
                "SK하이닉스",
                false
        );
    }

    @Test
    @DisplayName("shortCode로 종목을 조회하면 해당 종목을 반환한다")
    void testFindByShortCode_WithValidCode_ReturnsStock() {
        // given
        String shortCode = "005930";
        when(kospiMasterRepository.findByShortCode(eq(shortCode)))
                .thenReturn(Optional.of(samsungStock));

        // when
        Optional<KospiMaster> result = kospiMasterService.findByShortCode(shortCode);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getShortCode()).isEqualTo(shortCode);
        assertThat(result.get().getNameKr()).isEqualTo("삼성전자");
        verify(kospiMasterRepository).findByShortCode(eq(shortCode));
    }

    @Test
    @DisplayName("존재하지 않는 shortCode로 조회하면 Optional.empty를 반환한다")
    void testFindByShortCode_WithInvalidCode_ReturnsEmpty() {
        // given
        String invalidCode = "999999";
        when(kospiMasterRepository.findByShortCode(eq(invalidCode)))
                .thenReturn(Optional.empty());

        // when
        Optional<KospiMaster> result = kospiMasterService.findByShortCode(invalidCode);

        // then
        assertThat(result).isEmpty();
        verify(kospiMasterRepository).findByShortCode(eq(invalidCode));
    }

    @Test
    @DisplayName("shortCode가 정확히 일치하는 종목만 반환한다")
    void testFindByShortCode_ReturnsExactMatch() {
        // given
        String shortCode = "000660";
        when(kospiMasterRepository.findByShortCode(eq(shortCode)))
                .thenReturn(Optional.of(skHynixStock));

        // when
        Optional<KospiMaster> result = kospiMasterService.findByShortCode(shortCode);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getShortCode()).isEqualTo(shortCode);
        assertThat(result.get().getNameKr()).isEqualTo("SK하이닉스");
        verify(kospiMasterRepository).findByShortCode(eq(shortCode));
    }

    @Test
    @DisplayName("Repository가 정확히 한 번 호출된다")
    void testFindByShortCode_CallsRepositoryOnce() {
        // given
        String shortCode = "005930";
        when(kospiMasterRepository.findByShortCode(any()))
                .thenReturn(Optional.of(samsungStock));

        // when
        kospiMasterService.findByShortCode(shortCode);

        // then
        verify(kospiMasterRepository, times(1)).findByShortCode(any());
    }

    @Test
    @DisplayName("신규 종목 정보를 upsert하면 새로운 종목이 저장된다")
    void testUpsert_WithNewStock_SavesNewStock() {
        // given
        StockBaseInfo newStock = new StockBaseInfo("123456", "KR7123456789", "새종목");
        Map<String, StockBaseInfo> newStocks = Map.of("123456", newStock);

        when(kospiMasterRepository.findAll()).thenReturn(List.of());
        when(kospiMasterRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<KospiMaster> result = kospiMasterService.upsert(newStocks);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getShortCode()).isEqualTo("123456");
        assertThat(result.get(0).getNameKr()).isEqualTo("새종목");
        verify(kospiMasterRepository).findAll();
        verify(kospiMasterRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("기존 종목 정보를 upsert하면 종목 정보가 업데이트된다")
    void testUpsert_WithExistingStock_UpdatesStock() {
        // given
        StockBaseInfo updatedInfo = new StockBaseInfo("005930", "KR7005930003", "삼성전자(수정)");
        Map<String, StockBaseInfo> updatedStocks = Map.of("005930", updatedInfo);

        when(kospiMasterRepository.findAll()).thenReturn(List.of(samsungStock));
        when(kospiMasterRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<KospiMaster> result = kospiMasterService.upsert(updatedStocks);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNameKr()).isEqualTo("삼성전자(수정)");
        verify(kospiMasterRepository).findAll();
        verify(kospiMasterRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("상장폐지된 종목을 제거하면 isDeListed 상태가 업데이트된다")
    void testRemoveDeListed_UpdatesDeListedStatus() {
        // given
        StockBaseInfo activeStock = new StockBaseInfo("005930", "KR7005930003", "삼성전자");
        Map<String, StockBaseInfo> activeStocks = Map.of("005930", activeStock);

        when(kospiMasterRepository.findAll()).thenReturn(List.of(samsungStock, skHynixStock));

        // when
        kospiMasterService.removeDeListed(activeStocks);

        // then
        assertThat(samsungStock.isDeListed()).isFalse();
        assertThat(skHynixStock.isDeListed()).isTrue();
        verify(kospiMasterRepository).findAll();
    }

    @Test
    @DisplayName("여러 종목을 동시에 upsert할 수 있다")
    void testUpsert_WithMultipleStocks() {
        // given
        StockBaseInfo stock1 = new StockBaseInfo("111111", "KR7111111111", "종목1");
        StockBaseInfo stock2 = new StockBaseInfo("222222", "KR7222222222", "종목2");
        Map<String, StockBaseInfo> newStocks = Map.of(
                "111111", stock1,
                "222222", stock2
        );

        when(kospiMasterRepository.findAll()).thenReturn(List.of());
        when(kospiMasterRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<KospiMaster> result = kospiMasterService.upsert(newStocks);

        // then
        assertThat(result).hasSize(2);
        verify(kospiMasterRepository).saveAll(anyList());
    }
}
