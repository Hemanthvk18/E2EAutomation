package api.dtos.master;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.experimental.categories.Categories;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MasterCreateRequest {

    public Integer mainscopeId;
    public Integer masterDvpTypeId;
    public String masterTitle;

    //Product Line
    public List<Categories> categories = new ArrayList<>();

    //Responsibility
    //public String responsible;
    public List<CoResponsible> dvpCoResponsibles = new ArrayList<>();
    public String dvpResponsibleId;
    public String dvpResponsibleName;
    public String systemResponsibleEngineerId;
    public String systemResponsibleEngineerName;
    public String characteristicResponsibleEngineerId;
    public String characteristicResponsibleEngineerName;

    //Comments
    public String description = "";
    public String generalComment = "";

/*
Helper class and methods
 */

    public MasterCreateRequest addProductLines(Integer id) {
        if (id != null) {
            this.categories.add(new Categories(id));
        }
        return this;
    }


    public static class CoResponsible {
        public String loginName;
        public String fullName;

        public CoResponsible() {

        }

        public CoResponsible(String name, String fullName) {
            this.loginName = name;
            this.fullName = fullName;

        }
    }


    public class Categories {
        public Integer categoryId;

        public Categories() {
        }

        public Categories(Integer id) {
            this.categoryId = id;
        }
    }
}
