package com.xypai.content.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL POINT类型TypeHandler
 * 
 * 用于处理MySQL空间数据类型POINT与Java JTS Point对象之间的映射
 * 
 * 使用方法：
 * 在实体类字段上添加注解：
 * @TableField(value = "location", typeHandler = PointTypeHandler.class)
 * private Point location;
 *
 * @author Charlie (内容服务组)
 * @date 2025-01-15
 */
@MappedTypes(Point.class)
public class PointTypeHandler extends BaseTypeHandler<Point> {

    /**
     * 几何工厂（SRID 4326 = WGS84坐标系统）
     */
    private static final GeometryFactory GEOMETRY_FACTORY = 
        new GeometryFactory(new PrecisionModel(), 4326);
    
    /**
     * WKB读取器（Well-Known Binary格式）
     */
    private static final WKBReader WKB_READER = new WKBReader(GEOMETRY_FACTORY);
    
    /**
     * WKB写入器
     */
    private static final WKBWriter WKB_WRITER = new WKBWriter();

    /**
     * 设置非空参数
     * 将JTS Point对象转换为MySQL POINT类型
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Point parameter, JdbcType jdbcType) 
            throws SQLException {
        if (parameter == null) {
            ps.setBytes(i, null);
            return;
        }
        
        // 将Point转换为WKB字节数组
        byte[] wkb = WKB_WRITER.write(parameter);
        ps.setBytes(i, wkb);
    }

    /**
     * 根据列名获取结果
     */
    @Override
    public Point getNullableResult(ResultSet rs, String columnName) throws SQLException {
        byte[] wkb = rs.getBytes(columnName);
        return parsePoint(wkb);
    }

    /**
     * 根据列索引获取结果
     */
    @Override
    public Point getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] wkb = rs.getBytes(columnIndex);
        return parsePoint(wkb);
    }

    /**
     * 从CallableStatement获取结果
     */
    @Override
    public Point getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] wkb = cs.getBytes(columnIndex);
        return parsePoint(wkb);
    }

    /**
     * 解析WKB字节数组为Point对象
     * 
     * @param wkb WKB字节数组
     * @return Point对象，如果wkb为null则返回null
     */
    private Point parsePoint(byte[] wkb) {
        if (wkb == null || wkb.length == 0) {
            return null;
        }
        
        try {
            return (Point) WKB_READER.read(wkb);
        } catch (ParseException e) {
            throw new RuntimeException("解析MySQL POINT类型失败", e);
        }
    }

    /**
     * 创建Point对象的工具方法
     * 
     * @param longitude 经度
     * @param latitude 纬度
     * @return Point对象
     */
    public static Point createPoint(double longitude, double latitude) {
        Coordinate coordinate = new Coordinate(longitude, latitude);
        return GEOMETRY_FACTORY.createPoint(coordinate);
    }

    /**
     * 从Point对象获取经度
     * 
     * @param point Point对象
     * @return 经度，如果point为null则返回null
     */
    public static Double getLongitude(Point point) {
        return point != null ? point.getX() : null;
    }

    /**
     * 从Point对象获取纬度
     * 
     * @param point Point对象
     * @return 纬度，如果point为null则返回null
     */
    public static Double getLatitude(Point point) {
        return point != null ? point.getY() : null;
    }
}

