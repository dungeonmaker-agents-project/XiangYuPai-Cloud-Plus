package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.user.domain.vo.RegionVo;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 地区数据控制器
 * Region data Controller for city picker
 *
 * @author XiangYuPai
 * @since 2025-12-02
 */
@Slf4j
@Tag(name = "Region API", description = "地区数据接口")
@RestController
@RequestMapping("/api/common")
public class RegionController {

    // 省份数据
    private static final Map<String, String> PROVINCES = new LinkedHashMap<>();
    // 城市数据 (key: 省份code, value: 城市列表)
    private static final Map<String, List<RegionVo>> CITIES = new HashMap<>();

    static {
        // 初始化省份数据
        PROVINCES.put("110000", "北京");
        PROVINCES.put("120000", "天津");
        PROVINCES.put("310000", "上海");
        PROVINCES.put("500000", "重庆");
        PROVINCES.put("130000", "河北");
        PROVINCES.put("140000", "山西");
        PROVINCES.put("150000", "内蒙古");
        PROVINCES.put("210000", "辽宁");
        PROVINCES.put("220000", "吉林");
        PROVINCES.put("230000", "黑龙江");
        PROVINCES.put("320000", "江苏");
        PROVINCES.put("330000", "浙江");
        PROVINCES.put("340000", "安徽");
        PROVINCES.put("350000", "福建");
        PROVINCES.put("360000", "江西");
        PROVINCES.put("370000", "山东");
        PROVINCES.put("410000", "河南");
        PROVINCES.put("420000", "湖北");
        PROVINCES.put("430000", "湖南");
        PROVINCES.put("440000", "广东");
        PROVINCES.put("450000", "广西");
        PROVINCES.put("460000", "海南");
        PROVINCES.put("510000", "四川");
        PROVINCES.put("520000", "贵州");
        PROVINCES.put("530000", "云南");
        PROVINCES.put("540000", "西藏");
        PROVINCES.put("610000", "陕西");
        PROVINCES.put("620000", "甘肃");
        PROVINCES.put("630000", "青海");
        PROVINCES.put("640000", "宁夏");
        PROVINCES.put("650000", "新疆");
        PROVINCES.put("710000", "台湾");
        PROVINCES.put("810000", "香港");
        PROVINCES.put("820000", "澳门");

        // 初始化主要城市数据
        // 直辖市
        CITIES.put("110000", Arrays.asList(
            RegionVo.builder().code("110100").name("北京市").hasChildren(false).build()
        ));
        CITIES.put("120000", Arrays.asList(
            RegionVo.builder().code("120100").name("天津市").hasChildren(false).build()
        ));
        CITIES.put("310000", Arrays.asList(
            RegionVo.builder().code("310100").name("上海市").hasChildren(false).build()
        ));
        CITIES.put("500000", Arrays.asList(
            RegionVo.builder().code("500100").name("重庆市").hasChildren(false).build()
        ));

        // 广东省
        CITIES.put("440000", Arrays.asList(
            RegionVo.builder().code("440100").name("广州市").hasChildren(false).build(),
            RegionVo.builder().code("440300").name("深圳市").hasChildren(false).build(),
            RegionVo.builder().code("440400").name("珠海市").hasChildren(false).build(),
            RegionVo.builder().code("440600").name("佛山市").hasChildren(false).build(),
            RegionVo.builder().code("440700").name("江门市").hasChildren(false).build(),
            RegionVo.builder().code("440800").name("湛江市").hasChildren(false).build(),
            RegionVo.builder().code("440900").name("茂名市").hasChildren(false).build(),
            RegionVo.builder().code("441200").name("肇庆市").hasChildren(false).build(),
            RegionVo.builder().code("441300").name("惠州市").hasChildren(false).build(),
            RegionVo.builder().code("441400").name("梅州市").hasChildren(false).build(),
            RegionVo.builder().code("441500").name("汕尾市").hasChildren(false).build(),
            RegionVo.builder().code("441600").name("河源市").hasChildren(false).build(),
            RegionVo.builder().code("441700").name("阳江市").hasChildren(false).build(),
            RegionVo.builder().code("441800").name("清远市").hasChildren(false).build(),
            RegionVo.builder().code("441900").name("东莞市").hasChildren(false).build(),
            RegionVo.builder().code("442000").name("中山市").hasChildren(false).build(),
            RegionVo.builder().code("445100").name("潮州市").hasChildren(false).build(),
            RegionVo.builder().code("445200").name("揭阳市").hasChildren(false).build(),
            RegionVo.builder().code("445300").name("云浮市").hasChildren(false).build()
        ));

        // 江苏省
        CITIES.put("320000", Arrays.asList(
            RegionVo.builder().code("320100").name("南京市").hasChildren(false).build(),
            RegionVo.builder().code("320200").name("无锡市").hasChildren(false).build(),
            RegionVo.builder().code("320300").name("徐州市").hasChildren(false).build(),
            RegionVo.builder().code("320400").name("常州市").hasChildren(false).build(),
            RegionVo.builder().code("320500").name("苏州市").hasChildren(false).build(),
            RegionVo.builder().code("320600").name("南通市").hasChildren(false).build(),
            RegionVo.builder().code("320700").name("连云港市").hasChildren(false).build(),
            RegionVo.builder().code("320800").name("淮安市").hasChildren(false).build(),
            RegionVo.builder().code("320900").name("盐城市").hasChildren(false).build(),
            RegionVo.builder().code("321000").name("扬州市").hasChildren(false).build(),
            RegionVo.builder().code("321100").name("镇江市").hasChildren(false).build(),
            RegionVo.builder().code("321200").name("泰州市").hasChildren(false).build(),
            RegionVo.builder().code("321300").name("宿迁市").hasChildren(false).build()
        ));

        // 浙江省
        CITIES.put("330000", Arrays.asList(
            RegionVo.builder().code("330100").name("杭州市").hasChildren(false).build(),
            RegionVo.builder().code("330200").name("宁波市").hasChildren(false).build(),
            RegionVo.builder().code("330300").name("温州市").hasChildren(false).build(),
            RegionVo.builder().code("330400").name("嘉兴市").hasChildren(false).build(),
            RegionVo.builder().code("330500").name("湖州市").hasChildren(false).build(),
            RegionVo.builder().code("330600").name("绍兴市").hasChildren(false).build(),
            RegionVo.builder().code("330700").name("金华市").hasChildren(false).build(),
            RegionVo.builder().code("330800").name("衢州市").hasChildren(false).build(),
            RegionVo.builder().code("330900").name("舟山市").hasChildren(false).build(),
            RegionVo.builder().code("331000").name("台州市").hasChildren(false).build(),
            RegionVo.builder().code("331100").name("丽水市").hasChildren(false).build()
        ));

        // 四川省
        CITIES.put("510000", Arrays.asList(
            RegionVo.builder().code("510100").name("成都市").hasChildren(false).build(),
            RegionVo.builder().code("510300").name("自贡市").hasChildren(false).build(),
            RegionVo.builder().code("510400").name("攀枝花市").hasChildren(false).build(),
            RegionVo.builder().code("510500").name("泸州市").hasChildren(false).build(),
            RegionVo.builder().code("510600").name("德阳市").hasChildren(false).build(),
            RegionVo.builder().code("510700").name("绵阳市").hasChildren(false).build(),
            RegionVo.builder().code("510800").name("广元市").hasChildren(false).build(),
            RegionVo.builder().code("510900").name("遂宁市").hasChildren(false).build(),
            RegionVo.builder().code("511000").name("内江市").hasChildren(false).build(),
            RegionVo.builder().code("511100").name("乐山市").hasChildren(false).build(),
            RegionVo.builder().code("511300").name("南充市").hasChildren(false).build(),
            RegionVo.builder().code("511400").name("眉山市").hasChildren(false).build(),
            RegionVo.builder().code("511500").name("宜宾市").hasChildren(false).build(),
            RegionVo.builder().code("511600").name("广安市").hasChildren(false).build(),
            RegionVo.builder().code("511700").name("达州市").hasChildren(false).build(),
            RegionVo.builder().code("511800").name("雅安市").hasChildren(false).build(),
            RegionVo.builder().code("511900").name("巴中市").hasChildren(false).build(),
            RegionVo.builder().code("512000").name("资阳市").hasChildren(false).build()
        ));

        // 湖北省
        CITIES.put("420000", Arrays.asList(
            RegionVo.builder().code("420100").name("武汉市").hasChildren(false).build(),
            RegionVo.builder().code("420200").name("黄石市").hasChildren(false).build(),
            RegionVo.builder().code("420300").name("十堰市").hasChildren(false).build(),
            RegionVo.builder().code("420500").name("宜昌市").hasChildren(false).build(),
            RegionVo.builder().code("420600").name("襄阳市").hasChildren(false).build(),
            RegionVo.builder().code("420700").name("鄂州市").hasChildren(false).build(),
            RegionVo.builder().code("420800").name("荆门市").hasChildren(false).build(),
            RegionVo.builder().code("420900").name("孝感市").hasChildren(false).build(),
            RegionVo.builder().code("421000").name("荆州市").hasChildren(false).build(),
            RegionVo.builder().code("421100").name("黄冈市").hasChildren(false).build(),
            RegionVo.builder().code("421200").name("咸宁市").hasChildren(false).build(),
            RegionVo.builder().code("421300").name("随州市").hasChildren(false).build()
        ));

        // 湖南省
        CITIES.put("430000", Arrays.asList(
            RegionVo.builder().code("430100").name("长沙市").hasChildren(false).build(),
            RegionVo.builder().code("430200").name("株洲市").hasChildren(false).build(),
            RegionVo.builder().code("430300").name("湘潭市").hasChildren(false).build(),
            RegionVo.builder().code("430400").name("衡阳市").hasChildren(false).build(),
            RegionVo.builder().code("430500").name("邵阳市").hasChildren(false).build(),
            RegionVo.builder().code("430600").name("岳阳市").hasChildren(false).build(),
            RegionVo.builder().code("430700").name("常德市").hasChildren(false).build(),
            RegionVo.builder().code("430800").name("张家界市").hasChildren(false).build(),
            RegionVo.builder().code("430900").name("益阳市").hasChildren(false).build(),
            RegionVo.builder().code("431000").name("郴州市").hasChildren(false).build(),
            RegionVo.builder().code("431100").name("永州市").hasChildren(false).build(),
            RegionVo.builder().code("431200").name("怀化市").hasChildren(false).build(),
            RegionVo.builder().code("431300").name("娄底市").hasChildren(false).build()
        ));

        // 河南省
        CITIES.put("410000", Arrays.asList(
            RegionVo.builder().code("410100").name("郑州市").hasChildren(false).build(),
            RegionVo.builder().code("410200").name("开封市").hasChildren(false).build(),
            RegionVo.builder().code("410300").name("洛阳市").hasChildren(false).build(),
            RegionVo.builder().code("410400").name("平顶山市").hasChildren(false).build(),
            RegionVo.builder().code("410500").name("安阳市").hasChildren(false).build(),
            RegionVo.builder().code("410600").name("鹤壁市").hasChildren(false).build(),
            RegionVo.builder().code("410700").name("新乡市").hasChildren(false).build(),
            RegionVo.builder().code("410800").name("焦作市").hasChildren(false).build(),
            RegionVo.builder().code("410900").name("濮阳市").hasChildren(false).build(),
            RegionVo.builder().code("411000").name("许昌市").hasChildren(false).build(),
            RegionVo.builder().code("411100").name("漯河市").hasChildren(false).build(),
            RegionVo.builder().code("411200").name("三门峡市").hasChildren(false).build(),
            RegionVo.builder().code("411300").name("南阳市").hasChildren(false).build(),
            RegionVo.builder().code("411400").name("商丘市").hasChildren(false).build(),
            RegionVo.builder().code("411500").name("信阳市").hasChildren(false).build(),
            RegionVo.builder().code("411600").name("周口市").hasChildren(false).build(),
            RegionVo.builder().code("411700").name("驻马店市").hasChildren(false).build()
        ));

        // 山东省
        CITIES.put("370000", Arrays.asList(
            RegionVo.builder().code("370100").name("济南市").hasChildren(false).build(),
            RegionVo.builder().code("370200").name("青岛市").hasChildren(false).build(),
            RegionVo.builder().code("370300").name("淄博市").hasChildren(false).build(),
            RegionVo.builder().code("370400").name("枣庄市").hasChildren(false).build(),
            RegionVo.builder().code("370500").name("东营市").hasChildren(false).build(),
            RegionVo.builder().code("370600").name("烟台市").hasChildren(false).build(),
            RegionVo.builder().code("370700").name("潍坊市").hasChildren(false).build(),
            RegionVo.builder().code("370800").name("济宁市").hasChildren(false).build(),
            RegionVo.builder().code("370900").name("泰安市").hasChildren(false).build(),
            RegionVo.builder().code("371000").name("威海市").hasChildren(false).build(),
            RegionVo.builder().code("371100").name("日照市").hasChildren(false).build(),
            RegionVo.builder().code("371300").name("临沂市").hasChildren(false).build(),
            RegionVo.builder().code("371400").name("德州市").hasChildren(false).build(),
            RegionVo.builder().code("371500").name("聊城市").hasChildren(false).build(),
            RegionVo.builder().code("371600").name("滨州市").hasChildren(false).build(),
            RegionVo.builder().code("371700").name("菏泽市").hasChildren(false).build()
        ));

        // 福建省
        CITIES.put("350000", Arrays.asList(
            RegionVo.builder().code("350100").name("福州市").hasChildren(false).build(),
            RegionVo.builder().code("350200").name("厦门市").hasChildren(false).build(),
            RegionVo.builder().code("350300").name("莆田市").hasChildren(false).build(),
            RegionVo.builder().code("350400").name("三明市").hasChildren(false).build(),
            RegionVo.builder().code("350500").name("泉州市").hasChildren(false).build(),
            RegionVo.builder().code("350600").name("漳州市").hasChildren(false).build(),
            RegionVo.builder().code("350700").name("南平市").hasChildren(false).build(),
            RegionVo.builder().code("350800").name("龙岩市").hasChildren(false).build(),
            RegionVo.builder().code("350900").name("宁德市").hasChildren(false).build()
        ));

        // 安徽省
        CITIES.put("340000", Arrays.asList(
            RegionVo.builder().code("340100").name("合肥市").hasChildren(false).build(),
            RegionVo.builder().code("340200").name("芜湖市").hasChildren(false).build(),
            RegionVo.builder().code("340300").name("蚌埠市").hasChildren(false).build(),
            RegionVo.builder().code("340400").name("淮南市").hasChildren(false).build(),
            RegionVo.builder().code("340500").name("马鞍山市").hasChildren(false).build(),
            RegionVo.builder().code("340600").name("淮北市").hasChildren(false).build(),
            RegionVo.builder().code("340700").name("铜陵市").hasChildren(false).build(),
            RegionVo.builder().code("340800").name("安庆市").hasChildren(false).build(),
            RegionVo.builder().code("341000").name("黄山市").hasChildren(false).build(),
            RegionVo.builder().code("341100").name("滁州市").hasChildren(false).build(),
            RegionVo.builder().code("341200").name("阜阳市").hasChildren(false).build(),
            RegionVo.builder().code("341300").name("宿州市").hasChildren(false).build(),
            RegionVo.builder().code("341500").name("六安市").hasChildren(false).build(),
            RegionVo.builder().code("341600").name("亳州市").hasChildren(false).build(),
            RegionVo.builder().code("341700").name("池州市").hasChildren(false).build(),
            RegionVo.builder().code("341800").name("宣城市").hasChildren(false).build()
        ));

        // 江西省
        CITIES.put("360000", Arrays.asList(
            RegionVo.builder().code("360100").name("南昌市").hasChildren(false).build(),
            RegionVo.builder().code("360200").name("景德镇市").hasChildren(false).build(),
            RegionVo.builder().code("360300").name("萍乡市").hasChildren(false).build(),
            RegionVo.builder().code("360400").name("九江市").hasChildren(false).build(),
            RegionVo.builder().code("360500").name("新余市").hasChildren(false).build(),
            RegionVo.builder().code("360600").name("鹰潭市").hasChildren(false).build(),
            RegionVo.builder().code("360700").name("赣州市").hasChildren(false).build(),
            RegionVo.builder().code("360800").name("吉安市").hasChildren(false).build(),
            RegionVo.builder().code("360900").name("宜春市").hasChildren(false).build(),
            RegionVo.builder().code("361000").name("抚州市").hasChildren(false).build(),
            RegionVo.builder().code("361100").name("上饶市").hasChildren(false).build()
        ));

        // 河北省
        CITIES.put("130000", Arrays.asList(
            RegionVo.builder().code("130100").name("石家庄市").hasChildren(false).build(),
            RegionVo.builder().code("130200").name("唐山市").hasChildren(false).build(),
            RegionVo.builder().code("130300").name("秦皇岛市").hasChildren(false).build(),
            RegionVo.builder().code("130400").name("邯郸市").hasChildren(false).build(),
            RegionVo.builder().code("130500").name("邢台市").hasChildren(false).build(),
            RegionVo.builder().code("130600").name("保定市").hasChildren(false).build(),
            RegionVo.builder().code("130700").name("张家口市").hasChildren(false).build(),
            RegionVo.builder().code("130800").name("承德市").hasChildren(false).build(),
            RegionVo.builder().code("130900").name("沧州市").hasChildren(false).build(),
            RegionVo.builder().code("131000").name("廊坊市").hasChildren(false).build(),
            RegionVo.builder().code("131100").name("衡水市").hasChildren(false).build()
        ));

        // 山西省
        CITIES.put("140000", Arrays.asList(
            RegionVo.builder().code("140100").name("太原市").hasChildren(false).build(),
            RegionVo.builder().code("140200").name("大同市").hasChildren(false).build(),
            RegionVo.builder().code("140300").name("阳泉市").hasChildren(false).build(),
            RegionVo.builder().code("140400").name("长治市").hasChildren(false).build(),
            RegionVo.builder().code("140500").name("晋城市").hasChildren(false).build(),
            RegionVo.builder().code("140600").name("朔州市").hasChildren(false).build(),
            RegionVo.builder().code("140700").name("晋中市").hasChildren(false).build(),
            RegionVo.builder().code("140800").name("运城市").hasChildren(false).build(),
            RegionVo.builder().code("140900").name("忻州市").hasChildren(false).build(),
            RegionVo.builder().code("141000").name("临汾市").hasChildren(false).build(),
            RegionVo.builder().code("141100").name("吕梁市").hasChildren(false).build()
        ));

        // 辽宁省
        CITIES.put("210000", Arrays.asList(
            RegionVo.builder().code("210100").name("沈阳市").hasChildren(false).build(),
            RegionVo.builder().code("210200").name("大连市").hasChildren(false).build(),
            RegionVo.builder().code("210300").name("鞍山市").hasChildren(false).build(),
            RegionVo.builder().code("210400").name("抚顺市").hasChildren(false).build(),
            RegionVo.builder().code("210500").name("本溪市").hasChildren(false).build(),
            RegionVo.builder().code("210600").name("丹东市").hasChildren(false).build(),
            RegionVo.builder().code("210700").name("锦州市").hasChildren(false).build(),
            RegionVo.builder().code("210800").name("营口市").hasChildren(false).build(),
            RegionVo.builder().code("210900").name("阜新市").hasChildren(false).build(),
            RegionVo.builder().code("211000").name("辽阳市").hasChildren(false).build(),
            RegionVo.builder().code("211100").name("盘锦市").hasChildren(false).build(),
            RegionVo.builder().code("211200").name("铁岭市").hasChildren(false).build(),
            RegionVo.builder().code("211300").name("朝阳市").hasChildren(false).build(),
            RegionVo.builder().code("211400").name("葫芦岛市").hasChildren(false).build()
        ));

        // 吉林省
        CITIES.put("220000", Arrays.asList(
            RegionVo.builder().code("220100").name("长春市").hasChildren(false).build(),
            RegionVo.builder().code("220200").name("吉林市").hasChildren(false).build(),
            RegionVo.builder().code("220300").name("四平市").hasChildren(false).build(),
            RegionVo.builder().code("220400").name("辽源市").hasChildren(false).build(),
            RegionVo.builder().code("220500").name("通化市").hasChildren(false).build(),
            RegionVo.builder().code("220600").name("白山市").hasChildren(false).build(),
            RegionVo.builder().code("220700").name("松原市").hasChildren(false).build(),
            RegionVo.builder().code("220800").name("白城市").hasChildren(false).build()
        ));

        // 黑龙江省
        CITIES.put("230000", Arrays.asList(
            RegionVo.builder().code("230100").name("哈尔滨市").hasChildren(false).build(),
            RegionVo.builder().code("230200").name("齐齐哈尔市").hasChildren(false).build(),
            RegionVo.builder().code("230300").name("鸡西市").hasChildren(false).build(),
            RegionVo.builder().code("230400").name("鹤岗市").hasChildren(false).build(),
            RegionVo.builder().code("230500").name("双鸭山市").hasChildren(false).build(),
            RegionVo.builder().code("230600").name("大庆市").hasChildren(false).build(),
            RegionVo.builder().code("230700").name("伊春市").hasChildren(false).build(),
            RegionVo.builder().code("230800").name("佳木斯市").hasChildren(false).build(),
            RegionVo.builder().code("230900").name("七台河市").hasChildren(false).build(),
            RegionVo.builder().code("231000").name("牡丹江市").hasChildren(false).build(),
            RegionVo.builder().code("231100").name("黑河市").hasChildren(false).build(),
            RegionVo.builder().code("231200").name("绥化市").hasChildren(false).build()
        ));

        // 陕西省
        CITIES.put("610000", Arrays.asList(
            RegionVo.builder().code("610100").name("西安市").hasChildren(false).build(),
            RegionVo.builder().code("610200").name("铜川市").hasChildren(false).build(),
            RegionVo.builder().code("610300").name("宝鸡市").hasChildren(false).build(),
            RegionVo.builder().code("610400").name("咸阳市").hasChildren(false).build(),
            RegionVo.builder().code("610500").name("渭南市").hasChildren(false).build(),
            RegionVo.builder().code("610600").name("延安市").hasChildren(false).build(),
            RegionVo.builder().code("610700").name("汉中市").hasChildren(false).build(),
            RegionVo.builder().code("610800").name("榆林市").hasChildren(false).build(),
            RegionVo.builder().code("610900").name("安康市").hasChildren(false).build(),
            RegionVo.builder().code("611000").name("商洛市").hasChildren(false).build()
        ));

        // 甘肃省
        CITIES.put("620000", Arrays.asList(
            RegionVo.builder().code("620100").name("兰州市").hasChildren(false).build(),
            RegionVo.builder().code("620200").name("嘉峪关市").hasChildren(false).build(),
            RegionVo.builder().code("620300").name("金昌市").hasChildren(false).build(),
            RegionVo.builder().code("620400").name("白银市").hasChildren(false).build(),
            RegionVo.builder().code("620500").name("天水市").hasChildren(false).build(),
            RegionVo.builder().code("620600").name("武威市").hasChildren(false).build(),
            RegionVo.builder().code("620700").name("张掖市").hasChildren(false).build(),
            RegionVo.builder().code("620800").name("平凉市").hasChildren(false).build(),
            RegionVo.builder().code("620900").name("酒泉市").hasChildren(false).build(),
            RegionVo.builder().code("621000").name("庆阳市").hasChildren(false).build(),
            RegionVo.builder().code("621100").name("定西市").hasChildren(false).build(),
            RegionVo.builder().code("621200").name("陇南市").hasChildren(false).build()
        ));

        // 云南省
        CITIES.put("530000", Arrays.asList(
            RegionVo.builder().code("530100").name("昆明市").hasChildren(false).build(),
            RegionVo.builder().code("530300").name("曲靖市").hasChildren(false).build(),
            RegionVo.builder().code("530400").name("玉溪市").hasChildren(false).build(),
            RegionVo.builder().code("530500").name("保山市").hasChildren(false).build(),
            RegionVo.builder().code("530600").name("昭通市").hasChildren(false).build(),
            RegionVo.builder().code("530700").name("丽江市").hasChildren(false).build(),
            RegionVo.builder().code("530800").name("普洱市").hasChildren(false).build(),
            RegionVo.builder().code("530900").name("临沧市").hasChildren(false).build()
        ));

        // 贵州省
        CITIES.put("520000", Arrays.asList(
            RegionVo.builder().code("520100").name("贵阳市").hasChildren(false).build(),
            RegionVo.builder().code("520200").name("六盘水市").hasChildren(false).build(),
            RegionVo.builder().code("520300").name("遵义市").hasChildren(false).build(),
            RegionVo.builder().code("520400").name("安顺市").hasChildren(false).build(),
            RegionVo.builder().code("520500").name("毕节市").hasChildren(false).build(),
            RegionVo.builder().code("520600").name("铜仁市").hasChildren(false).build()
        ));

        // 广西
        CITIES.put("450000", Arrays.asList(
            RegionVo.builder().code("450100").name("南宁市").hasChildren(false).build(),
            RegionVo.builder().code("450200").name("柳州市").hasChildren(false).build(),
            RegionVo.builder().code("450300").name("桂林市").hasChildren(false).build(),
            RegionVo.builder().code("450400").name("梧州市").hasChildren(false).build(),
            RegionVo.builder().code("450500").name("北海市").hasChildren(false).build(),
            RegionVo.builder().code("450600").name("防城港市").hasChildren(false).build(),
            RegionVo.builder().code("450700").name("钦州市").hasChildren(false).build(),
            RegionVo.builder().code("450800").name("贵港市").hasChildren(false).build(),
            RegionVo.builder().code("450900").name("玉林市").hasChildren(false).build(),
            RegionVo.builder().code("451000").name("百色市").hasChildren(false).build(),
            RegionVo.builder().code("451100").name("贺州市").hasChildren(false).build(),
            RegionVo.builder().code("451200").name("河池市").hasChildren(false).build(),
            RegionVo.builder().code("451300").name("来宾市").hasChildren(false).build(),
            RegionVo.builder().code("451400").name("崇左市").hasChildren(false).build()
        ));

        // 海南省
        CITIES.put("460000", Arrays.asList(
            RegionVo.builder().code("460100").name("海口市").hasChildren(false).build(),
            RegionVo.builder().code("460200").name("三亚市").hasChildren(false).build(),
            RegionVo.builder().code("460300").name("三沙市").hasChildren(false).build(),
            RegionVo.builder().code("460400").name("儋州市").hasChildren(false).build()
        ));

        // 内蒙古
        CITIES.put("150000", Arrays.asList(
            RegionVo.builder().code("150100").name("呼和浩特市").hasChildren(false).build(),
            RegionVo.builder().code("150200").name("包头市").hasChildren(false).build(),
            RegionVo.builder().code("150300").name("乌海市").hasChildren(false).build(),
            RegionVo.builder().code("150400").name("赤峰市").hasChildren(false).build(),
            RegionVo.builder().code("150500").name("通辽市").hasChildren(false).build(),
            RegionVo.builder().code("150600").name("鄂尔多斯市").hasChildren(false).build(),
            RegionVo.builder().code("150700").name("呼伦贝尔市").hasChildren(false).build(),
            RegionVo.builder().code("150800").name("巴彦淖尔市").hasChildren(false).build(),
            RegionVo.builder().code("150900").name("乌兰察布市").hasChildren(false).build()
        ));

        // 西藏
        CITIES.put("540000", Arrays.asList(
            RegionVo.builder().code("540100").name("拉萨市").hasChildren(false).build(),
            RegionVo.builder().code("540200").name("日喀则市").hasChildren(false).build(),
            RegionVo.builder().code("540300").name("昌都市").hasChildren(false).build(),
            RegionVo.builder().code("540400").name("林芝市").hasChildren(false).build(),
            RegionVo.builder().code("540500").name("山南市").hasChildren(false).build(),
            RegionVo.builder().code("540600").name("那曲市").hasChildren(false).build()
        ));

        // 青海省
        CITIES.put("630000", Arrays.asList(
            RegionVo.builder().code("630100").name("西宁市").hasChildren(false).build(),
            RegionVo.builder().code("630200").name("海东市").hasChildren(false).build()
        ));

        // 宁夏
        CITIES.put("640000", Arrays.asList(
            RegionVo.builder().code("640100").name("银川市").hasChildren(false).build(),
            RegionVo.builder().code("640200").name("石嘴山市").hasChildren(false).build(),
            RegionVo.builder().code("640300").name("吴忠市").hasChildren(false).build(),
            RegionVo.builder().code("640400").name("固原市").hasChildren(false).build(),
            RegionVo.builder().code("640500").name("中卫市").hasChildren(false).build()
        ));

        // 新疆
        CITIES.put("650000", Arrays.asList(
            RegionVo.builder().code("650100").name("乌鲁木齐市").hasChildren(false).build(),
            RegionVo.builder().code("650200").name("克拉玛依市").hasChildren(false).build(),
            RegionVo.builder().code("650400").name("吐鲁番市").hasChildren(false).build(),
            RegionVo.builder().code("650500").name("哈密市").hasChildren(false).build()
        ));

        // 台湾
        CITIES.put("710000", Arrays.asList(
            RegionVo.builder().code("710100").name("台北市").hasChildren(false).build(),
            RegionVo.builder().code("710200").name("高雄市").hasChildren(false).build(),
            RegionVo.builder().code("710300").name("台中市").hasChildren(false).build(),
            RegionVo.builder().code("710400").name("台南市").hasChildren(false).build(),
            RegionVo.builder().code("710500").name("新北市").hasChildren(false).build(),
            RegionVo.builder().code("710600").name("桃园市").hasChildren(false).build()
        ));

        // 香港
        CITIES.put("810000", Arrays.asList(
            RegionVo.builder().code("810100").name("香港岛").hasChildren(false).build(),
            RegionVo.builder().code("810200").name("九龙").hasChildren(false).build(),
            RegionVo.builder().code("810300").name("新界").hasChildren(false).build()
        ));

        // 澳门
        CITIES.put("820000", Arrays.asList(
            RegionVo.builder().code("820100").name("澳门半岛").hasChildren(false).build(),
            RegionVo.builder().code("820200").name("离岛").hasChildren(false).build()
        ));
    }

    /**
     * 获取地区列表
     * parentCode 为空时返回省份列表，否则返回该省份下的城市列表
     */
    @Operation(summary = "Get region list")
    @GetMapping("/regions")
    public R<List<RegionVo>> getRegions(@RequestParam(required = false) String parentCode) {
        List<RegionVo> regions = new ArrayList<>();

        if (parentCode == null || parentCode.isEmpty()) {
            // 返回省份列表
            for (Map.Entry<String, String> entry : PROVINCES.entrySet()) {
                boolean hasChildren = CITIES.containsKey(entry.getKey()) && !CITIES.get(entry.getKey()).isEmpty();
                regions.add(RegionVo.builder()
                    .code(entry.getKey())
                    .name(entry.getValue())
                    .hasChildren(hasChildren)
                    .build());
            }
        } else {
            // 返回城市列表
            List<RegionVo> cities = CITIES.get(parentCode);
            if (cities != null) {
                regions.addAll(cities);
            }
        }

        return R.ok(regions);
    }

    /**
     * 获取所有省份列表
     */
    @Operation(summary = "Get all provinces")
    @GetMapping("/regions/provinces")
    public R<List<RegionVo>> getProvinces() {
        return getRegions(null);
    }

    /**
     * 根据省份编码获取城市列表
     */
    @Operation(summary = "Get cities by province code")
    @GetMapping("/regions/cities")
    public R<List<RegionVo>> getCities(@RequestParam String provinceCode) {
        return getRegions(provinceCode);
    }
}
