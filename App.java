package demo;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {

    private final DeepSeekClient client = new DeepSeekClient();

    @Override
    public void start(Stage stage) {
        TextArea input = new TextArea();
        input.setPromptText("请输入 JavaFX 界面需求，例如：登录界面");

        TextArea output = new TextArea();
        output.setEditable(false);

        Button genBtn = new Button("生成代码");
        Label status = new Label("就绪");

        genBtn.setOnAction(e -> {
            genBtn.setDisable(true);
            status.setText("生成中…");

            Task<String> task = new Task<String>()
            {
                @Override
                protected String call() throws Exception {
                    return client.generateJavaFxCode(input.getText());
                }
            };

            task.setOnSucceeded(ev -> {
                output.setText(task.getValue());
                status.setText("完成");
                genBtn.setDisable(false);
            });

            task.setOnFailed(ev -> {
                output.setText(task.getException().toString());
                status.setText("失败");
                genBtn.setDisable(false);
            });

            new Thread(task).start();
        });

        VBox root = new VBox(10,
                new Label("需求输入"),
                input,
                genBtn,
                new Label("生成的 JavaFX 代码"),
                output,
                status
        );
        root.setPadding(new javafx.geometry.Insets(10));

        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("AI JavaFX 代码生成 Demo");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
