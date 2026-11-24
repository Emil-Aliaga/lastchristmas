
package lastchristmas;

import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.*;

public class LastChristmas {

    static final int AJUSTE_MS = 0;
    static final int TAMANO_IMAGEN = 220;
    static final int TAMANO_LETRA = 20;

    static final String CARPETA =
        "C:/Users/EMIL/Desktop/gatos navidad/";

    static class Frase {

        long tiempo;
        String texto;

        Frase(long tiempo, String texto) {
            this.tiempo = tiempo;
            this.texto = texto;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> iniciar());
    }

    public static ImageIcon cargarImagen(String ruta) {

        ImageIcon original = new ImageIcon(ruta);

        if (original.getIconWidth() <= 0 ||
                original.getIconHeight() <= 0) {
            return null;
        }

        double escala = Math.min(
            (double) TAMANO_IMAGEN / original.getIconWidth(),
            (double) TAMANO_IMAGEN / original.getIconHeight()
        );

        int ancho = Math.max(1, (int) (
            original.getIconWidth() * escala
        ));

        int alto = Math.max(1, (int) (
            original.getIconHeight() * escala
        ));

        return new ImageIcon(
            original.getImage().getScaledInstance(
                ancho, alto, Image.SCALE_SMOOTH
            )
        );
    }
    
// Detectar si una linea contiene una frase real
public static boolean esFraseCompleta(String texto) {

    String limpio = texto.toLowerCase(
        java.util.Locale.ROOT
    );

    // Eliminar signos de puntuacion
    limpio = limpio.replaceAll("[^a-z\\s]", " ");

    // Eliminar vocalizaciones
    limpio = limpio.replaceAll(
        "\\b(a+h*|o+h*|wo+a+h*|who+a+h*|"
        + "ye+a+h*|o+o+h*|u+h+|h+m+|m+m+|"
        + "la+|na+|hey+)\\b", ""
    );

    limpio = limpio.trim();

    if (limpio.isEmpty()) {
        return false;
    }

    
    return limpio.split("\\s+").length >= 2;
}

    public static void iniciar() {

        try {

            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );

            ArrayList<ImageIcon> memes = new ArrayList<>();

        String[] extensiones = {
        ".jpg", ".jpeg", ".png", ".gif"
        };

for (int i = 1; i <= 100; i++) {

    for (String extension : extensiones) {

        File archivoImagen = new File(
            CARPETA + "gato" + i + extension
        );

        if (archivoImagen.exists()) {

            ImageIcon meme = cargarImagen(
                archivoImagen.getAbsolutePath()
            );

            if (meme != null) {

                memes.add(meme);

                System.out.println(
                    "Imagen cargada: "
                    + archivoImagen.getName()
                );

                break;
            }
        }
    }
}
            if (memes.size() < 2) {
                throw new Exception(
                    "Necesitas al menos dos imagenes de gatos."
                );
            }

            System.out.println(
                "Memes cargados: " + memes.size()
            );

            ArrayList<Frase> frases = new ArrayList<>();

            Pattern patron = Pattern.compile(
                "\\[(\\d+):(\\d+(?:\\.\\d+)?)\\]"
            );

            for (String linea : Files.readAllLines(
                    new File(
                        CARPETA + "lastchristmas.lrc"
                    ).toPath(),
                    StandardCharsets.UTF_8)) {

                Matcher matcher = patron.matcher(linea);

                String texto = patron.matcher(linea)
                    .replaceAll("").trim();

                while (matcher.find()) {

                    int minutos = Integer.parseInt(
                        matcher.group(1)
                    );

                    double segundos = Double.parseDouble(
                        matcher.group(2)
                    );

                    long tiempo = Math.round(
                        (minutos * 60 + segundos) * 1000
                    );

                    frases.add(new Frase(tiempo, texto));
                }
            }

            // Ordenar frases cronologicamente
            frases.sort(
                Comparator.comparingLong(f -> f.tiempo)
            );

            if (frases.isEmpty()) {
                throw new Exception(
                    "El archivo LRC esta vacio"
                );
            }

            // ============================
            // CARGAR MUSICA
            // ============================

            File archivo = new File(
                CARPETA + "Wham! - Last Christmas.wav"
            );

            Clip musica = AudioSystem.getClip();

            try (AudioInputStream audio =
                    AudioSystem.getAudioInputStream(archivo)) {

                musica.open(audio);
            }

            JLabel foto = new JLabel(memes.get(0));

            foto.setPreferredSize(
                new Dimension(TAMANO_IMAGEN, TAMANO_IMAGEN)
            );

            foto.setHorizontalAlignment(JLabel.CENTER);
            foto.setVerticalAlignment(JLabel.CENTER);

            JTextArea letra = new JTextArea();

            letra.setFont(
                new Font("Segoe UI", Font.PLAIN, TAMANO_LETRA)
            );

            letra.setLineWrap(true);
            letra.setWrapStyleWord(true);
            letra.setEditable(false);
            letra.setOpaque(false);
            letra.setFocusable(false);

            letra.setPreferredSize(
                new Dimension(300, 120)
            );

            JPanel panel = new JPanel(
                new BorderLayout(25, 10)
            );

            panel.setBorder(
                BorderFactory.createEmptyBorder(
                    10, 10, 10, 10
                )
            );

            panel.add(foto, BorderLayout.WEST);

            JPanel textoPanel = new JPanel(
                new GridBagLayout()
            );

            textoPanel.add(letra);

            panel.add(
                textoPanel, BorderLayout.CENTER
            );

            // Crear ventana emergente
            JOptionPane ventana = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION
            );

            JDialog dialogo = ventana.createDialog(
                null, "Last Christmas - WHAM!"
            );

            dialogo.setSize(650, 350);
            dialogo.setLocationRelativeTo(null);
            dialogo.setResizable(false);

            final int[] ultimaFrase = {-2};
            final int[] memeActual = {-1};

            Timer sincronizador = new Timer(20, e -> {

                long posicion =
                    musica.getMicrosecondPosition() / 1000
                    - AJUSTE_MS;

                int indice = -1;

                for (int i = 0; i < frases.size(); i++) {

                    if (posicion >= frases.get(i).tiempo) {
                        indice = i;
                    } else {
                        break;
                    }
                }

                if (indice != ultimaFrase[0]) {

                    ultimaFrase[0] = indice;

                    String texto = indice >= 0
                        ? frases.get(indice).texto
                        : "";

                    letra.setText(texto);

                    // Cambiar meme solamente en frases completas
                    if (esFraseCompleta(texto)) {

                    memeActual[0] =
                        (memeActual[0] + 1) % memes.size();

                     foto.setIcon(
                        memes.get(memeActual[0])
                    );

                     foto.repaint();
                    }
                }

                if (musica.getLongFramePosition()
                        >= musica.getFrameLength()) {

                    ((Timer) e.getSource()).stop();

                    dialogo.dispose();
                }
            });

            try {

                musica.start();
                sincronizador.start();

                dialogo.setVisible(true);

            } finally {

                sincronizador.stop();
                musica.stop();
                musica.close();
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                null,
                "Error: " + e.getMessage()
            );
        }
    }
}
