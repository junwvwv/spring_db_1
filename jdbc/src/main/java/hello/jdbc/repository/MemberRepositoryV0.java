package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

@Slf4j
public class MemberRepositoryV0 {

    //JDBC DriverManager 사용
    public Member save(Member member) throws SQLException {

        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = getConnection(); //데이터베이스 커넥션 획득
            pstmt = conn.prepareStatement(sql); //sql 준비
            pstmt.setString(1, member.getMemberId()); //1번째 ? 에 값을 바인딩 - SQL 인젝션 공격 예방
            pstmt.setInt(2, member.getMoney()); //2번째 ? 에 값을 바인딩
            pstmt.executeUpdate(); //준비된 SQL 을 실제로 데이터베이스에 전달
            return member;
        }catch (SQLException e){
            log.error("db error", e);
            throw  e;
        }finally{
            close(conn, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {

        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery(); //조회할 땐 executeQuery - 결과를 ResultSet 에 담아서 반환한다
            //ResultSet 내부에 있는 커서를 이동해서 다음 데이터를 조회할 수 있다
            if (rs.next()) { //호출 시 커서가 다음으로 이동한다. 다음 커서에 데이터가 없으면 false
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {

        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {

        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    //리소스 정리를 하지 않으면 커넥션이 부족해질 수 있음
    private void close(Connection conn, Statement stmt, ResultSet rs){

        if(rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if(stmt != null){
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }


    private Connection getConnection(){
        return DBConnectionUtil.getConnection();
    }

}
