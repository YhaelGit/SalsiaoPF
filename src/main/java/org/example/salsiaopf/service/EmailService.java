package org.example.salsiaopf.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Envío de facturas por correo (Jakarta Mail / SMTP configurable).
 */
public final class EmailService {

    private static final String PROPS_PATH = "/org/example/salsiaopf/ventas-mail.properties";

    private EmailService() {
    }

    public static boolean estaConfigurado() {
        Properties p = cargarPropiedades();
        return !p.getProperty("mail.smtp.host", "").trim().isEmpty()
                && !p.getProperty("mail.smtp.user", "").trim().isEmpty();
    }

    /**
     * @param idVenta ID numérico de la venta (asunto: Factura Salsiao #ID)
     * @return mensaje de resultado
     */
    public static String enviarFactura(String emailDestino, Path pdf, int idVenta, double total) {
        if (emailDestino == null || emailDestino.isBlank()) {
            return "Correo no indicado.";
        }
        if (!emailDestino.contains("@")) {
            return "Correo inválido.";
        }
        if (pdf == null || !pdf.toFile().exists()) {
            return "Archivo PDF no encontrado.";
        }
        if (!estaConfigurado()) {
            return "SMTP no configurado. Edite ventas-mail.properties. PDF guardado en facturas/.";
        }

        Properties props = cargarPropiedades();
        String host = props.getProperty("mail.smtp.host");
        String port = props.getProperty("mail.smtp.port", "587");
        String user = props.getProperty("mail.smtp.user");
        String pass = props.getProperty("mail.smtp.password", "");
        String from = props.getProperty("mail.smtp.from", user);
        boolean tls = Boolean.parseBoolean(props.getProperty("mail.smtp.starttls", "true"));

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "Salsiao"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(emailDestino));
            message.setSubject("Factura Salsiao #" + idVenta);

            MimeBodyPart texto = new MimeBodyPart();
            texto.setText(String.format(
                    "Estimado cliente,\n\nAdjuntamos su factura de Salsiao.\n"
                            + "Número de venta: %d\nTotal: RD$ %,.2f\n\n¡Gracias por su preferencia!",
                    idVenta, total));

            MimeBodyPart adjunto = new MimeBodyPart();
            adjunto.attachFile(pdf.toFile());

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(texto);
            multipart.addBodyPart(adjunto);
            message.setContent(multipart);

            Transport.send(message);
            return "Correo enviado a " + emailDestino;

        } catch (MessagingException | IOException e) {
            System.out.println("[EmailService] " + e.getMessage());
            return "Error al enviar correo: " + e.getMessage();
        }
    }

    private static Properties cargarPropiedades() {
        Properties props = new Properties();
        try (InputStream in = EmailService.class.getResourceAsStream(PROPS_PATH)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.out.println("[EmailService] No se cargó ventas-mail.properties");
        }
        return props;
    }
}
