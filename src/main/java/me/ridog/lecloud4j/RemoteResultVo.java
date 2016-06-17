package me.ridog.lecloud4j;

/**
 * @author: Tate
 * @date: 2016/6/17 10:49
 */
public class RemoteResultVo {

    private Integer rs;
    private String jsonObj;

    public String getJsonObj() {
        return jsonObj;
    }

    public void setJsonObj(String jsonObj) {
        this.jsonObj = jsonObj;
    }

    public Integer getRs() {
        return rs;
    }

    public void setRs(Integer rs) {
        this.rs = rs;
    }

    @Override
    public String toString() {
        return "RemoteResultVo{" +
                "rs=" + rs +
                ", jsonObj='" + jsonObj + '\'' +
                '}';
    }
}
