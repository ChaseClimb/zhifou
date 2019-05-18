package com.wenda.dao;

import com.wenda.model.LoginTicket;
import com.wenda.model.admin.AdminLoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminLoginTicketDao {
    String TABLE_NAME = " admin_login_ticket ";
    String INSERT_FIELDS = " user_id,expired,status,ticket ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values(#{userId},#{expired},#{status},#{ticket})"})
    int addTicket(LoginTicket ticket);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where ticket=#{ticket}"})
    AdminLoginTicket selectIdByTicket(String ticket);

    //将ticket置为无效
    @Update({"update ",TABLE_NAME," set status=#{status} where ticket=#{ticket}"})
    void updateStatus(@Param("ticket") String ticket, @Param("status") int status);
}
