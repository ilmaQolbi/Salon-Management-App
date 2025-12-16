package Controller;

import DAO.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Controller for Lapor Kendala Dialog.
 * This is a reusable component that can be called from Kasir or Karyawan.
 */
public class LaporKendalaController {

    @FXML
    private TextArea txtIsiLaporan;

    private Stage dialogStage;
    private String currentUserId;
    private boolean submitted = false;

    /**
     * Static method to show the dialog.
     * 
     * @param userId The ID of the user submitting the report.
     * @return true if report was submitted successfully.
     */
    public static boolean showDialog(String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    LaporKendalaController.class.getResource("/View/LaporKendalaDialog.fxml"));
            Parent root = loader.load();

            LaporKendalaController controller = loader.getController();
            controller.setCurrentUserId(userId);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Laporkan Kendala");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            return controller.isSubmitted();

        } catch (IOException e) {
            e.printStackTrace();
            tampilkanPeringatan(Alert.AlertType.ERROR, "Error", "Gagal membuka dialog laporan kendala.");
            return false;
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    @FXML
    void handleKirim(ActionEvent event) {
        String isiLaporan = txtIsiLaporan.getText();

        if (isiLaporan == null || isiLaporan.trim().isEmpty()) {
            tampilkanPeringatan(Alert.AlertType.WARNING, "Peringatan", "Isi laporan tidak boleh kosong!");
            return;
        }

        // Simpan ke Database
        if (currentUserId == null || currentUserId.isEmpty()) {
            currentUserId = "UNKNOWN";
        }

        String query = "INSERT INTO laporan_kendala (id_user, isi_laporan, tanggal, status) VALUES (?, ?, NOW(), 'Pending')";
        int result = DatabaseManager.eksekusiUpdate(query, currentUserId, isiLaporan.trim());

        if (result > 0) {
            tampilkanPeringatan(Alert.AlertType.INFORMATION, "Terkirim", "Laporan kendala berhasil dikirim!");
            submitted = true;
            dialogStage.close();
        } else {
            tampilkanPeringatan(Alert.AlertType.ERROR, "Gagal", "Gagal mengirim laporan. Silakan coba lagi.");
        }
    }

    @FXML
    void handleBatal(ActionEvent event) {
        dialogStage.close();
    }

    private static void tampilkanPeringatan(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
