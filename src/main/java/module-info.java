module com.solitaire.solitaire {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.solitaire.solitaire to javafx.fxml;
    exports com.solitaire.solitaire;
}