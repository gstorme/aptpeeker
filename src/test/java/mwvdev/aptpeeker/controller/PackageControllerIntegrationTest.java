package mwvdev.aptpeeker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mwvdev.aptpeeker.PackageTestData;
import mwvdev.aptpeeker.model.NotificationError;
import mwvdev.aptpeeker.model.NotificationResult;
import mwvdev.aptpeeker.service.NotificationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = PackageController.class, secure = false)
public class PackageControllerIntegrationTest {

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void canHandleUpdates() throws Exception {
        List<String> packages = PackageTestData.getPackages();

        NotificationResult expectedNotificationResult = new NotificationResult(true);
        when(notificationService.sendNotification(packages)).thenReturn(expectedNotificationResult);

        mvc.perform(post("/api/package/updates")
                .content(objectMapper.writeValueAsString(packages))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void handleUpdates_WhenSendingNotificationFails_ReturnsInternalServerError() throws Exception {
        NotificationResult notificationResult = new NotificationResult(new NotificationError("notificationResult"));
        when(notificationService.sendNotification(any())).thenReturn(notificationResult);

        mvc.perform(post("/api/package/updates")
                .content(objectMapper.writeValueAsString(PackageTestData.getPackages()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void handleUpdates_WhenEmptyCollectionPostBody_ReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/package/updates")
                .content(objectMapper.writeValueAsString(new String[] {}))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void handleUpdates_WhenEmptyStringPostBody_ReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/package/updates")
                .content("")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void handleUpdates_WhenMissingPostBody_ReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/package/updates")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}