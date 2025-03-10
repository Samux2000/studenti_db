package studenti_db;
import java.sql.*;
import java.util.Scanner;

public class StudentiDB {
    private static final String URL = "jdbc:mysql://localhost:3306/studenti_db";
    private static final String USER = "root";  // Cambia con il tuo username MySQL
    private static final String PASSWORD = "";  // Inserisci la tua password MySQL

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            int choice;
            do {
                System.out.println("\n--- MENU ---");
                System.out.println("1. Inserisci nuovo studente");
                System.out.println("2. Iscrivi studente a un corso (tramite ID)");
                System.out.println("3. Mostra corsi di uno studente");
                System.out.println("0. Esci");
                System.out.print("Scelta: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consuma newline

                switch (choice) {
                    case 1:
                        System.out.print("Inserisci nome studente: ");
                        String studentName = scanner.nextLine();
                        insertStudent(studentName);
                        break;
                    case 2:
                        System.out.print("Inserisci ID studente: ");
                        int studentId = scanner.nextInt();
                        System.out.print("Inserisci ID corso: ");
                        int courseId = scanner.nextInt();
                        enrollStudent(studentId, courseId);
                        break;
                    case 3:
                        System.out.print("Inserisci ID studente: ");
                        int studId = scanner.nextInt();
                        showStudentCourses(studId);
                        break;
                    case 0:
                        System.out.println("Uscita dal programma...");
                        break;
                    default:
                        System.out.println("Scelta non valida, riprova.");
                }
            } while (choice != 0);
        }
    }

    // Metodo per inserire uno studente
    private static void insertStudent(String nome) {
        String sql = "INSERT INTO studenti (nome) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nome);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        System.out.println("Studente aggiunto con successo! ID assegnato: " + id);
                    }
                }
            } else {
                System.out.println("Errore nell'inserimento.");
            }
        } catch (SQLException e) {
            System.err.println("Errore SQL: " + e.getMessage());
        }
    }

    // Metodo per iscrivere uno studente a un corso (usando ID)
    private static void enrollStudent(int studentId, int courseId) {
        if (!studentExists(studentId)) {
            System.out.println("Errore: Lo studente con ID " + studentId + " non esiste.");
            return;
        }
        if (!courseExists(courseId)) {
            System.out.println("Errore: Il corso con ID " + courseId + " non esiste.");
            return;
        }

        String sql = "INSERT INTO iscrizioni (id_studente, id_corso) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0 ? "Iscrizione avvenuta con successo!" : "Errore durante l'iscrizione.");
        } catch (SQLException e) {
            System.err.println("Errore SQL: " + e.getMessage());
        }
    }

    // Metodo per mostrare tutti i corsi di uno studente
    private static void showStudentCourses(int studentId) {
        String sql = "SELECT c.nome FROM corsi c " +
                     "JOIN iscrizioni i ON c.id = i.id_corso " +
                     "WHERE i.id_studente = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\nCorsi frequentati dallo studente con ID " + studentId + ":");
                boolean found = false;
                while (rs.next()) {
                    System.out.println("- " + rs.getString("nome"));
                    found = true;
                }
                if (!found) {
                    System.out.println("Nessun corso trovato.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore SQL: " + e.getMessage());
        }
    }

    // Metodo per verificare se uno studente esiste
    private static boolean studentExists(int studentId) {
        String sql = "SELECT id FROM studenti WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Errore SQL: " + e.getMessage());
        }
        return false;
    }

    // Metodo per verificare se un corso esiste
    private static boolean courseExists(int courseId) {
        String sql = "SELECT id FROM corsi WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Errore SQL: " + e.getMessage());
        }
        return false;
    }
}

