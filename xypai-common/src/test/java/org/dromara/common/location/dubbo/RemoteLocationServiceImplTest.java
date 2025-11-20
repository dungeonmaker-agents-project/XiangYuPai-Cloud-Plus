package org.dromara.common.location.dubbo;

import org.dromara.common.api.location.domain.CityInfoVo;
import org.dromara.common.api.location.domain.DistanceVo;
import org.dromara.common.api.location.domain.LocationPointDto;
import org.dromara.common.core.domain.R;
import org.dromara.common.location.service.ICityService;
import org.dromara.common.location.service.ILocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RemoteLocationService Dubbo实现类单元测试
 * Remote Location Service Implementation Unit Tests
 *
 * @author XiangYuPai Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("位置服务RPC实现测试")
public class RemoteLocationServiceImplTest {

    @Mock
    private ILocationService locationService;

    @Mock
    private ICityService cityService;

    @InjectMocks
    private RemoteLocationServiceImpl remoteLocationService;

    private BigDecimal validLat;
    private BigDecimal validLng;
    private BigDecimal targetLat;
    private BigDecimal targetLng;

    @BeforeEach
    void setUp() {
        // 北京坐标
        validLat = BigDecimal.valueOf(39.904989);
        validLng = BigDecimal.valueOf(116.405285);

        // 上海坐标
        targetLat = BigDecimal.valueOf(31.230416);
        targetLng = BigDecimal.valueOf(121.473701);
    }

    @Test
    @DisplayName("计算距离 - 正常情况")
    void testCalculateDistance_Success() {
        // Given
        BigDecimal expectedDistance = BigDecimal.valueOf(1067.89);
        when(locationService.validateCoordinates(any(), any())).thenReturn(true);
        when(locationService.calculateDistance(validLat, validLng, targetLat, targetLng))
            .thenReturn(expectedDistance);
        when(locationService.formatDistance(expectedDistance)).thenReturn("1067.89km");

        // When
        R<DistanceVo> result = remoteLocationService.calculateDistance(
            validLat, validLng, targetLat, targetLng
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getDistance()).isEqualByComparingTo(expectedDistance);
        assertThat(result.getData().getUnit()).isEqualTo("km");
        assertThat(result.getData().getDisplayText()).isEqualTo("1067.89km");

        // Verify
        verify(locationService, times(2)).validateCoordinates(any(), any());
        verify(locationService).calculateDistance(validLat, validLng, targetLat, targetLng);
        verify(locationService).formatDistance(expectedDistance);
    }

    @Test
    @DisplayName("计算距离 - 起点坐标无效")
    void testCalculateDistance_InvalidFromCoordinates() {
        // Given
        when(locationService.validateCoordinates(validLat, validLng)).thenReturn(false);

        // When
        R<DistanceVo> result = remoteLocationService.calculateDistance(
            validLat, validLng, targetLat, targetLng
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isFalse();
        assertThat(result.getMsg()).contains("起点坐标无效");

        // Verify
        verify(locationService).validateCoordinates(validLat, validLng);
        verify(locationService, never()).calculateDistance(any(), any(), any(), any());
    }

    @Test
    @DisplayName("计算距离 - 终点坐标无效")
    void testCalculateDistance_InvalidToCoordinates() {
        // Given
        when(locationService.validateCoordinates(validLat, validLng)).thenReturn(true);
        when(locationService.validateCoordinates(targetLat, targetLng)).thenReturn(false);

        // When
        R<DistanceVo> result = remoteLocationService.calculateDistance(
            validLat, validLng, targetLat, targetLng
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isFalse();
        assertThat(result.getMsg()).contains("终点坐标无效");

        // Verify
        verify(locationService, times(2)).validateCoordinates(any(), any());
        verify(locationService, never()).calculateDistance(any(), any(), any(), any());
    }

    @Test
    @DisplayName("批量计算距离 - 正常情况")
    void testCalculateBatchDistance_Success() {
        // Given
        LocationPointDto point1 = new LocationPointDto();
        point1.setId(1001L);
        point1.setLatitude(targetLat);
        point1.setLongitude(targetLng);

        LocationPointDto point2 = new LocationPointDto();
        point2.setId(1002L);
        point2.setLatitude(BigDecimal.valueOf(22.543099));
        point2.setLongitude(BigDecimal.valueOf(114.057868));

        List<LocationPointDto> targets = Arrays.asList(point1, point2);

        when(locationService.validateCoordinates(validLat, validLng)).thenReturn(true);
        when(locationService.calculateDistance(eq(validLat), eq(validLng), any(), any()))
            .thenReturn(BigDecimal.valueOf(1067.89))
            .thenReturn(BigDecimal.valueOf(1945.12));
        when(locationService.formatDistance(any()))
            .thenReturn("1067.89km")
            .thenReturn("1945.12km");

        // When
        R<List<DistanceVo>> result = remoteLocationService.calculateBatchDistance(
            validLat, validLng, targets
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isTrue();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getId()).isEqualTo(1001L);
        assertThat(result.getData().get(1).getId()).isEqualTo(1002L);

        // Verify
        verify(locationService).validateCoordinates(validLat, validLng);
        verify(locationService, times(2)).calculateDistance(any(), any(), any(), any());
    }

    @Test
    @DisplayName("批量计算距离 - 目标列表为空")
    void testCalculateBatchDistance_EmptyTargets() {
        // When
        R<List<DistanceVo>> result = remoteLocationService.calculateBatchDistance(
            validLat, validLng, null
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isTrue();
        assertThat(result.getData()).isEmpty();

        // Verify
        verify(locationService, never()).calculateDistance(any(), any(), any(), any());
    }

    @Test
    @DisplayName("批量计算距离 - 起点坐标无效")
    void testCalculateBatchDistance_InvalidFromCoordinates() {
        // Given
        LocationPointDto point = new LocationPointDto();
        point.setId(1001L);
        point.setLatitude(targetLat);
        point.setLongitude(targetLng);

        when(locationService.validateCoordinates(validLat, validLng)).thenReturn(false);

        // When
        R<List<DistanceVo>> result = remoteLocationService.calculateBatchDistance(
            validLat, validLng, Arrays.asList(point)
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isFalse();
        assertThat(result.getMsg()).contains("起点坐标无效");
    }

    @Test
    @DisplayName("验证坐标 - 有效坐标")
    void testValidateCoordinates_Valid() {
        // Given
        when(locationService.validateCoordinates(validLat, validLng)).thenReturn(true);

        // When
        R<Boolean> result = remoteLocationService.validateCoordinates(validLat, validLng);

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(locationService).validateCoordinates(validLat, validLng);
    }

    @Test
    @DisplayName("验证坐标 - 无效坐标")
    void testValidateCoordinates_Invalid() {
        // Given
        BigDecimal invalidLat = BigDecimal.valueOf(100); // 超出范围
        when(locationService.validateCoordinates(invalidLat, validLng)).thenReturn(false);

        // When
        R<Boolean> result = remoteLocationService.validateCoordinates(invalidLat, validLng);

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isTrue();
        assertThat(result.getData()).isFalse();
    }

    @Test
    @DisplayName("获取城市信息 - 正常情况")
    void testGetCityInfo_Success() {
        // Given
        String cityCode = "110100";
        org.dromara.common.location.domain.vo.CityInfoVo businessVo =
            new org.dromara.common.location.domain.vo.CityInfoVo();
        businessVo.setId(1L);
        businessVo.setCityCode(cityCode);
        businessVo.setCityName("北京");
        businessVo.setProvince("北京市");
        businessVo.setPinyin("beijing");
        businessVo.setFirstLetter("B");
        businessVo.setCenterLat(validLat);
        businessVo.setCenterLng(validLng);
        businessVo.setIsHot(1);

        when(cityService.getByCityCode(cityCode)).thenReturn(businessVo);

        // When
        R<CityInfoVo> result = remoteLocationService.getCityInfo(cityCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getCityCode()).isEqualTo(cityCode);
        assertThat(result.getData().getCityName()).isEqualTo("北京");
        assertThat(result.getData().getFirstLetter()).isEqualTo("B");

        // Verify
        verify(cityService).getByCityCode(cityCode);
    }

    @Test
    @DisplayName("获取城市信息 - 城市不存在")
    void testGetCityInfo_NotFound() {
        // Given
        String cityCode = "999999";
        when(cityService.getByCityCode(cityCode)).thenReturn(null);

        // When
        R<CityInfoVo> result = remoteLocationService.getCityInfo(cityCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isFalse();
        assertThat(result.getMsg()).contains("城市不存在");

        // Verify
        verify(cityService).getByCityCode(cityCode);
    }

    @Test
    @DisplayName("根据城市名查询代码 - 功能未实现")
    void testGetCityCodeByName_NotImplemented() {
        // When
        R<String> result = remoteLocationService.getCityCodeByName("北京");

        // Then
        assertThat(result).isNotNull();
        assertThat(R.isSuccess(result)).isFalse();
        assertThat(result.getMsg()).contains("功能未实现");
    }
}
