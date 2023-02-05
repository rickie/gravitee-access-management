/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.service.utils;

import io.gravitee.am.common.email.Email;
import io.gravitee.am.service.exception.TechnicalManagementException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.activation.MimetypesFileTypeMap;

/**
 * Utility class created to avoid duplication between {@link
 * io.gravitee.am.service.impl.EmailServiceImpl} and the SmtpResourceProvider
 *
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);

    private JavaMailSender mailSender;

    private String templatesPath;

    public EmailSender(JavaMailSender mailSender, String templatesPath) {
        this.mailSender = mailSender;
        this.templatesPath = templatesPath;
    }

    public void send(Email email) {
        try {
            MimeMessageHelper mailMessage =
                    new MimeMessageHelper(
                            mailSender.createMimeMessage(), true, StandardCharsets.UTF_8.name());
            String subject = email.getSubject();
            String content = email.getContent();
            String from = email.getFrom();
            String[] to = email.getTo();

            String fromName = email.getFromName();
            if (fromName == null || fromName.isEmpty()) {
                mailMessage.setFrom(from);
            } else {
                mailMessage.setFrom(from, fromName);
            }

            mailMessage.setTo(to);
            mailMessage.setSubject(subject);

            String html = addResourcesInMessage(mailMessage, content);
            LOGGER.debug(
                    "Sending an email to: {}\nSubject: {}\nMessage: {}",
                    email.getTo(),
                    email.getSubject(),
                    html);
            mailSender.send(mailMessage.getMimeMessage());
        } catch (Exception ex) {
            LOGGER.error("Error while creating email", ex);
            throw new TechnicalManagementException("Error while creating email", ex);
        }
    }

    private String addResourcesInMessage(MimeMessageHelper mailMessage, String htmlText)
            throws Exception {
        Document document = Jsoup.parse(htmlText);

        List<String> resources = new ArrayList<>();

        Elements imageElements = document.getElementsByTag("img");
        resources.addAll(
                imageElements.stream()
                        .filter(imageElement -> imageElement.hasAttr("src"))
                        .filter(imageElement -> !imageElement.attr("src").startsWith("http"))
                        .map(
                                imageElement -> {
                                    String src = imageElement.attr("src");
                                    imageElement.attr("src", "cid:" + src);
                                    return src;
                                })
                        .collect(Collectors.toList()));

        String html = document.html();
        mailMessage.setText(html, true);

        for (String res : resources) {
            if (res.startsWith("data:image/")) {
                String value = res.replaceFirst("^data:image/[^;]*;base64,?", "");
                byte[] bytes = Base64.getDecoder().decode(value.getBytes("UTF-8"));
                mailMessage.addInline(res, new ByteArrayResource(bytes), extractMimeType(res));
            } else {
                File file = new File(templatesPath, res);
                if (file.getCanonicalPath().startsWith(templatesPath)) {
                    FileSystemResource templateResource = new FileSystemResource(file);
                    mailMessage.addInline(res, templateResource, getContentTypeByFileName(res));
                } else {
                    LOGGER.warn("Resource path invalid : {}", file.getPath());
                }
            }
        }

        return html;
    }

    private String getContentTypeByFileName(String fileName) {
        if (fileName == null) {
            return "";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        }
        return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
    }

    /**
     * Extract the MIME type from a base64 string
     *
     * @param encoded Base64 string
     * @return MIME type string
     */
    private static String extractMimeType(String encoded) {
        Pattern mime = Pattern.compile("^data:([a-zA-Z0-9]+/[a-zA-Z0-9]+).*,.*");
        Matcher matcher = mime.matcher(encoded);
        if (!matcher.find()) return "";
        return matcher.group(1).toLowerCase();
    }
}
