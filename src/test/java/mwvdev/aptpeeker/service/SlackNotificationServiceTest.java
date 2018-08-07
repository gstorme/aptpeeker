package mwvdev.aptpeeker.service;

import mwvdev.aptpeeker.PackageTestData;
import mwvdev.aptpeeker.model.NotificationError;
import mwvdev.aptpeeker.model.NotificationResult;
import mwvdev.aptpeeker.model.SlackNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SlackNotificationServiceTest {

    private NotificationService notificationService;

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @MockBean
    private RestTemplate restTemplate;

    private SlackNotificationProperties slackNotificationProperties = new SlackNotificationProperties();

    @Before
    public void setUp() {
        slackNotificationProperties.setEndpoint("https://example.org/");

        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        notificationService = new SlackNotificationService(restTemplateBuilder, slackNotificationProperties);
    }

    @Test
    public void canSendNotification() {
        when(restTemplate.postForEntity(eq(slackNotificationProperties.getEndpoint()), any(HttpEntity.class), eq(String.class))).thenReturn(ResponseEntity.ok().build());

        NotificationResult notificationResult = notificationService.sendNotification(PackageTestData.getPackages());

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(slackNotificationProperties.getEndpoint()), entityCaptor.capture(), eq(String.class));

        assertThat(notificationResult, is(notNullValue()));
        assertThat(notificationResult.isSuccess(), is(true));
        assertThat(notificationResult.getError(), is(nullValue()));

        HttpEntity entity = entityCaptor.getValue();
        assertThat(entity, is(notNullValue()));
        Object entityBody = entity.getBody();
        assertThat(entityBody, is(notNullValue()));
        assertThat(entityBody, instanceOf(SlackNotification.class));

        SlackNotification slackNotification = (SlackNotification) entityBody;

        String expectedText =
                "Updates are available for the following packages:\n\n" +
                "libsystemd0 229-4ubuntu21.2\n" +
                "libpam-systemd 229-4ubuntu21.2\n" +
                "systemd 229-4ubuntu21.2";
        assertThat(slackNotification.getText(), is(expectedText));
    }

    @Test
    public void sendNotification_WhenNotificationFailed_ReturnsError() {
        String expectedMessage = "invalid_payload";
        when(restTemplate.postForEntity(any(String.class), any(), any())).thenReturn(ResponseEntity.badRequest().body(expectedMessage));

        NotificationResult notificationResult = notificationService.sendNotification(PackageTestData.getPackages());
        assertThat(notificationResult, is(notNullValue()));

        NotificationError error = notificationResult.getError();
        assertThat(error, is(notNullValue()));
        assertThat(error.getMessage(), is(expectedMessage));
    }

}
