package controller;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import db.DataBase;
import domain.Dispatcher;
import domain.Request;
import domain.Response;
import domain.ResponseBody;
import model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserController implements Controller{
    private static final UserController instance = new UserController();
    private final Dispatcher dispatcher = Dispatcher.getInstance();

    private UserController() {
    }

    @Override
    public void registerAll() {
        this.dispatcher.register("/user/create", "GET", this::createByGet);
        this.dispatcher.register("/user/create", "POST", this::createByPost);
        this.dispatcher.register("/user/list", "GET", this::list);
    }

    public Response createByGet(Request request) {
        DataBase.addUser(new User(
                request.getQuery("userId"),
                request.getQuery("password"),
                request.getQuery("name"),
                request.getQuery("email")
        ));
        return Response.ofRedirect("/index.html");
    }

    public Response createByPost(Request request) {
        DataBase.addUser(new User(
                request.getBody("userId"),
                request.getBody("password"),
                request.getBody("name"),
                request.getBody("email")
        ));
        return Response.ofRedirect("/index.html");
    }

    public Response list(Request request) {
        if (!request.getCookies().getValueOf("logined").equals("true")) {
            return Response.ofRedirect("/user/login.html");
        }

        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".html");
        Handlebars handlebars = new Handlebars(loader);
        Template template;
        ResponseBody body = null;
        try {
            template = handlebars.compile("user/list");
            List<User> users = new ArrayList<>(DataBase.findAll());
            Map<String, Object> m = new HashMap<String, Object>() {{
                put("users", new ArrayList<HashMap<String, Object>>(
                        IntStream.range(0, users.size()).mapToObj(
                                index -> new HashMap<String, Object>() {{
                                    put("index", index + 1);
                                    put("value", users.get(index));
                                }}
                        ).collect(Collectors.toList())));
            }};
            body = new ResponseBody(template.apply(m));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ofDynamicHtml(body);
    }

    public static UserController getInstance() {
        return instance;
    }
}
