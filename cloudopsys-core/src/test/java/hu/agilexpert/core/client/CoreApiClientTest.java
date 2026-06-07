package hu.agilexpert.core.client;

import hu.agilexpert.core.dto.ApplicationDto;
import hu.agilexpert.core.dto.UserApplicationDto;
import hu.agilexpert.core.dto.UserAssetDto;
import hu.agilexpert.core.dto.UserDto;
import hu.agilexpert.core.model.UserAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CoreApiClientTest {

    private MockRestServiceServer server;
    private CoreApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8090");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CoreApiClient(builder);
    }

    // --- listUsers ---

    @Test
    void listUsers_returnsListOfDtos() {
        server.expect(requestTo("http://localhost:8090/api/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"id\":1,\"name\":\"Alice\",\"username\":\"alice\",\"theme\":\"BRIGHT\",\"activeIconId\":null,\"activeBackgroundId\":null}]",
                        MediaType.APPLICATION_JSON));

        List<UserDto> users = client.listUsers();

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().username()).isEqualTo("alice");
        server.verify();
    }

    // --- findUserById ---

    @Test
    void findUserById_returnsDto_whenFound() {
        server.expect(requestTo("http://localhost:8090/api/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":1,\"name\":\"Alice\",\"username\":\"alice\",\"theme\":\"BRIGHT\",\"activeIconId\":null,\"activeBackgroundId\":null}",
                        MediaType.APPLICATION_JSON));

        Optional<UserDto> result = client.findUserById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Alice");
        server.verify();
    }

    @Test
    void findUserById_returnsEmpty_on404() {
        server.expect(requestTo("http://localhost:8090/api/users/99"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<UserDto> result = client.findUserById(99L);

        assertThat(result).isEmpty();
        server.verify();
    }

    // --- addUser ---

    @Test
    void addUser_returnsDto_onSuccess() {
        server.expect(requestTo("http://localhost:8090/api/users"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED).body(
                        "{\"id\":1,\"name\":\"Alice\",\"username\":\"alice\",\"theme\":\"BRIGHT\",\"activeIconId\":null,\"activeBackgroundId\":null}")
                        .contentType(MediaType.APPLICATION_JSON));

        Optional<UserDto> result = client.addUser("Alice", "alice");

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("alice");
        server.verify();
    }

    @Test
    void addUser_returnsEmpty_on409() {
        server.expect(requestTo("http://localhost:8090/api/users"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        Optional<UserDto> result = client.addUser("Alice", "alice");

        assertThat(result).isEmpty();
        server.verify();
    }

    // --- listAllApplications ---

    @Test
    void listAllApplications_returnsList() {
        server.expect(requestTo("http://localhost:8090/api/applications"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"id\":1,\"name\":\"Excel\"}]",
                        MediaType.APPLICATION_JSON));

        List<ApplicationDto> apps = client.listAllApplications();

        assertThat(apps).hasSize(1);
        assertThat(apps.getFirst().name()).isEqualTo("Excel");
        server.verify();
    }

    // --- getAppsForUser ---

    @Test
    void getAppsForUser_returnsList() {
        server.expect(requestTo("http://localhost:8090/api/users/1/apps"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"id\":1,\"userId\":1,\"application\":{\"id\":1,\"name\":\"Excel\"},\"running\":false}]",
                        MediaType.APPLICATION_JSON));

        List<UserApplicationDto> apps = client.getAppsForUser(1L);

        assertThat(apps).hasSize(1);
        assertThat(apps.getFirst().application().name()).isEqualTo("Excel");
        server.verify();
    }

    // --- getAssetsByUser ---

    @Test
    void getAssetsByUser_returnsList() {
        server.expect(requestTo("http://localhost:8090/api/users/1/assets"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"id\":1,\"type\":\"ICON\",\"fileName\":\"icon.png\",\"visibility\":\"PRIVATE\"}]",
                        MediaType.APPLICATION_JSON));

        List<UserAssetDto> assets = client.getAssetsByUser(1L);

        assertThat(assets).hasSize(1);
        assertThat(assets.getFirst().fileName()).isEqualTo("icon.png");
        assertThat(assets.getFirst().type()).isEqualTo(UserAsset.AssetType.ICON);
        server.verify();
    }

    // --- addIcon ---

    @Test
    void addIcon_returnsDto_onSuccess() {
        server.expect(requestTo("http://localhost:8090/api/users/1/assets/icons"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED).body(
                        "{\"id\":1,\"type\":\"ICON\",\"fileName\":\"icon.png\",\"visibility\":\"PRIVATE\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        Optional<UserAssetDto> result = client.addIcon(1L, "icon.png");

        assertThat(result).isPresent();
        assertThat(result.get().fileName()).isEqualTo("icon.png");
        server.verify();
    }

    @Test
    void addIcon_returnsEmpty_on409() {
        server.expect(requestTo("http://localhost:8090/api/users/1/assets/icons"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        Optional<UserAssetDto> result = client.addIcon(1L, "icon.png");

        assertThat(result).isEmpty();
        server.verify();
    }

    // --- startApp / stopApp ---

    @Test
    void startApp_sendsPostRequest() {
        server.expect(requestTo("http://localhost:8090/api/users/1/apps/2/start"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        client.startApp(1L, 2L);

        server.verify();
    }

    @Test
    void stopApp_sendsPostRequest() {
        server.expect(requestTo("http://localhost:8090/api/users/1/apps/2/stop"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        client.stopApp(1L, 2L);

        server.verify();
    }

    // --- deleteUser ---

    @Test
    void deleteUser_sendsDeleteRequest() {
        server.expect(requestTo("http://localhost:8090/api/users/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        client.deleteUser(1L);

        server.verify();
    }
}
