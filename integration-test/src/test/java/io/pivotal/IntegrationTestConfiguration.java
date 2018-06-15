/*
 * Copyright 2018-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import io.pivotal.reactor.scheduler.ReactorSchedulerClient;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.organizationquotadefinitions.CreateOrganizationQuotaDefinitionRequest;
import org.cloudfoundry.client.v2.organizations.AssociateOrganizationManagerRequest;
import org.cloudfoundry.client.v2.organizations.CreateOrganizationRequest;
import org.cloudfoundry.client.v2.spaces.CreateSpaceRequest;
import org.cloudfoundry.networking.NetworkingClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateServiceKeyRequest;
import org.cloudfoundry.operations.services.GetServiceKeyRequest;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.ProxyConfiguration;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.networking.ReactorNetworkingClient;
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.uaa.clients.CreateClientRequest;
import org.cloudfoundry.uaa.groups.AddMemberRequest;
import org.cloudfoundry.uaa.groups.CreateGroupRequest;
import org.cloudfoundry.uaa.groups.CreateGroupResponse;
import org.cloudfoundry.uaa.groups.Group;
import org.cloudfoundry.uaa.groups.ListGroupsRequest;
import org.cloudfoundry.uaa.groups.ListGroupsResponse;
import org.cloudfoundry.uaa.groups.MemberType;
import org.cloudfoundry.uaa.users.CreateUserRequest;
import org.cloudfoundry.uaa.users.CreateUserResponse;
import org.cloudfoundry.uaa.users.Email;
import org.cloudfoundry.uaa.users.Name;
import org.cloudfoundry.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.fail;
import static org.cloudfoundry.uaa.tokens.GrantType.AUTHORIZATION_CODE;
import static org.cloudfoundry.uaa.tokens.GrantType.CLIENT_CREDENTIALS;
import static org.cloudfoundry.uaa.tokens.GrantType.PASSWORD;
import static org.cloudfoundry.uaa.tokens.GrantType.REFRESH_TOKEN;
import static org.cloudfoundry.util.tuple.TupleUtils.function;

@Configuration
@EnableAutoConfiguration
public class IntegrationTestConfiguration {

    private static final List<String> GROUPS = Arrays.asList(
        "clients.admin",
        "clients.secret",
        "cloud_controller.admin");

    private static final List<String> SCOPES = Arrays.asList(
        "cloud_controller.admin",
        "cloud_controller.read",
        "cloud_controller.write");

    private final Logger logger = LoggerFactory.getLogger("cloudfoundry-client.test");

    @Bean
    @Qualifier("admin")
    ReactorCloudFoundryClient adminCloudFoundryClient(ConnectionContext connectionContext, @Value("${test.admin.password}") String password, @Value("${test.admin.username}") String username) {
        return ReactorCloudFoundryClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(PasswordGrantTokenProvider.builder()
                .password(password)
                .username(username)
                .build())
            .build();
    }

    @Bean
    @Qualifier("admin")
    NetworkingClient adminNetworkingClient(ConnectionContext connectionContext, @Value("${test.admin.password}") String password, @Value("${test.admin.username}") String username) {
        return ReactorNetworkingClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(PasswordGrantTokenProvider.builder()
                .password(password)
                .username(username)
                .build())
            .build();
    }

    @Bean
    @Qualifier("admin")
    ReactorUaaClient adminUaaClient(ConnectionContext connectionContext, @Value("${test.admin.clientId}") String clientId, @Value("${test.admin.clientSecret}") String clientSecret) {
        return ReactorUaaClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(ClientCredentialsGrantTokenProvider.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build())
            .build();
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<Tuple2<String, String>> client(@Qualifier("admin") UaaClient uaaClient, String clientId, String clientSecret) {
        return uaaClient.clients()
            .create(CreateClientRequest.builder()
                .authorizedGrantTypes(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, PASSWORD, REFRESH_TOKEN)
                .autoApprove(String.valueOf(true))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUriPattern("https://test.com/login")
                .scopes(SCOPES)
                .build())
            .thenReturn(Tuples.of(clientId, clientSecret))
            .doOnSubscribe(s -> this.logger.debug(">> CLIENT ({}/{}) <<", clientId, clientSecret))
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(r -> this.logger.debug("<< CLIENT ({})>>", clientId));
    }

    @Bean
    String clientId(NameFactory nameFactory) {
        return nameFactory.getClientId();
    }

    @Bean
    String clientSecret(NameFactory nameFactory) {
        return nameFactory.getClientSecret();
    }

    @Bean(initMethod = "clean", destroyMethod = "clean")
    CloudFoundryCleaner cloudFoundryCleaner(@Qualifier("admin") CloudFoundryClient cloudFoundryClient, NameFactory nameFactory, @Qualifier("admin") NetworkingClient networkingClient,
                                            @Qualifier("admin") UaaClient uaaClient) {

        return new CloudFoundryCleaner(cloudFoundryClient, nameFactory, networkingClient, uaaClient);
    }

    @Bean
    ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean
    @DependsOn({"organizationId", "spaceId"})
    DefaultCloudFoundryOperations cloudFoundryOperations(CloudFoundryClient cloudFoundryClient, String organizationName, String spaceName) {
        return DefaultCloudFoundryOperations.builder()
            .cloudFoundryClient(cloudFoundryClient)
            .organization(organizationName)
            .space(spaceName)
            .build();
    }

    @Bean
    DefaultConnectionContext connectionContext(@Value("${test.apiHost}") String apiHost,
                                               @Value("${test.proxy.host:}") String proxyHost,
                                               @Value("${test.proxy.password:}") String proxyPassword,
                                               @Value("${test.proxy.port:8080}") Integer proxyPort,
                                               @Value("${test.proxy.username:}") String proxyUsername,
                                               @Value("${test.skipSslValidation:false}") Boolean skipSslValidation) {

        DefaultConnectionContext.Builder connectionContext = DefaultConnectionContext.builder()
            .apiHost(apiHost)
            .problemHandler(new FailingDeserializationProblemHandler())  // Test-only problem handler
            .skipSslValidation(skipSslValidation)
            .sslHandshakeTimeout(Duration.ofSeconds(30));

        if (StringUtils.hasText(proxyHost)) {
            ProxyConfiguration.Builder proxyConfiguration = ProxyConfiguration.builder()
                .host(proxyHost)
                .port(proxyPort);

            if (StringUtils.hasText(proxyUsername)) {
                proxyConfiguration
                    .password(proxyPassword)
                    .username(proxyUsername);
            }

            connectionContext.proxyConfiguration(proxyConfiguration.build());
        }

        return connectionContext.build();
    }

    @Bean
    RandomNameFactory nameFactory(Random random) {
        return new RandomNameFactory(random);
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<String> organizationId(CloudFoundryClient cloudFoundryClient, String organizationName, String organizationQuotaName, Mono<String> userId) {
        return userId
            .flatMap(userId1 -> cloudFoundryClient.organizationQuotaDefinitions()
                .create(CreateOrganizationQuotaDefinitionRequest.builder()
                    .applicationInstanceLimit(-1)
                    .applicationTaskLimit(-1)
                    .instanceMemoryLimit(-1)
                    .memoryLimit(8192)
                    .name(organizationQuotaName)
                    .nonBasicServicesAllowed(true)
                    .totalPrivateDomains(-1)
                    .totalReservedRoutePorts(-1)
                    .totalRoutes(-1)
                    .totalServiceKeys(-1)
                    .totalServices(-1)
                    .build())
                .map(ResourceUtils::getId)
                .zipWith(Mono.just(userId1)))
            .flatMap(function((quotaId, userId1) -> cloudFoundryClient.organizations()
                .create(CreateOrganizationRequest.builder()
                    .name(organizationName)
                    .quotaDefinitionId(quotaId)
                    .build())
                .map(ResourceUtils::getId)
                .zipWith(Mono.just(userId1))))
            .flatMap(function((organizationId, userId1) -> cloudFoundryClient.organizations()
                .associateManager(AssociateOrganizationManagerRequest.builder()
                    .organizationId(organizationId)
                    .managerId(userId1)
                    .build())
                .thenReturn(organizationId)))
            .doOnSubscribe(s -> this.logger.debug(">> ORGANIZATION ({}) <<", organizationName))
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(id -> this.logger.debug("<< ORGANIZATION ({}) >>", id))
            .cache();
    }

    @Bean
    String organizationName(NameFactory nameFactory) {
        return nameFactory.getOrganizationName();
    }

    @Bean
    String organizationQuotaName(NameFactory nameFactory) {
        return nameFactory.getQuotaDefinitionName();
    }

    @Bean
    String password(NameFactory nameFactory) {
        return nameFactory.getPassword();
    }

    @Bean
    SecureRandom random() {
        return new SecureRandom();
    }

    @Bean
    @DependsOn("schedulerServiceKey")
    String schedulerApiEndpoint(CloudFoundryOperations cloudFoundryOperations, String schedulerServiceInstanceName, String schedulerServiceKeyName) {
        return cloudFoundryOperations.services()
            .getServiceKey(GetServiceKeyRequest.builder()
                .serviceInstanceName(schedulerServiceInstanceName)
                .serviceKeyName(schedulerServiceKeyName)
                .build())
            .map(serviceKey -> (String) serviceKey.getCredentials().get("api_endpoint"))
            .block();
    }

    @Bean
    ReactorSchedulerClient schedulerClient(ConnectionContext connectionContext, String schedulerApiEndpoint, TokenProvider tokenProvider) {
        return ReactorSchedulerClient.builder()
            .connectionContext(connectionContext)
            .root(Mono.just(schedulerApiEndpoint))
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean(initMethod = "block")
    Mono<Void> schedulerServiceInstance(CloudFoundryOperations cloudFoundryOperations, String schedulerServiceInstanceName) {
        return cloudFoundryOperations.services()
            .createInstance(CreateServiceInstanceRequest.builder()
                .planName("standard")
                .serviceInstanceName(schedulerServiceInstanceName)
                .serviceName("scheduler-for-pcf")
                .build());
    }

    @Bean
    String schedulerServiceInstanceName(NameFactory nameFactory) {
        return nameFactory.getServiceInstanceName();
    }

    @Bean(initMethod = "block")
    @DependsOn("schedulerServiceInstance")
    Mono<Void> schedulerServiceKey(CloudFoundryOperations cloudFoundryOperations, String schedulerServiceInstanceName, String schedulerServiceKeyName) {
        return cloudFoundryOperations.services()
            .createServiceKey(CreateServiceKeyRequest.builder()
                .serviceInstanceName(schedulerServiceInstanceName)
                .serviceKeyName(schedulerServiceKeyName)
                .build());
    }

    @Bean
    String schedulerServiceKeyName(NameFactory nameFactory) {
        return nameFactory.getServiceKeyName();
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<String> spaceId(CloudFoundryClient cloudFoundryClient, Mono<String> organizationId, String spaceName) {
        return organizationId
            .flatMap(orgId -> cloudFoundryClient.spaces()
                .create(CreateSpaceRequest.builder()
                    .name(spaceName)
                    .organizationId(orgId)
                    .build()))
            .map(ResourceUtils::getId)
            .doOnSubscribe(s -> this.logger.debug(">> SPACE ({}) <<", spaceName))
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(id -> this.logger.debug("<< SPACE ({}) >>", id))
            .cache();
    }

    @Bean
    String spaceName(NameFactory nameFactory) {
        return nameFactory.getSpaceName();
    }

    @Bean
    @DependsOn({"client", "userId"})
    PasswordGrantTokenProvider tokenProvider(String clientId, String clientSecret, String password, String username) {
        return PasswordGrantTokenProvider.builder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .password(password)
            .username(username)
            .build();
    }

    @Bean(initMethod = "block")
    @DependsOn("cloudFoundryCleaner")
    Mono<String> userId(@Qualifier("admin") UaaClient uaaClient, String password, String username) {
        return uaaClient.users()
            .create(CreateUserRequest.builder()
                .email(Email.builder()
                    .primary(true)
                    .value(String.format("%s@%s.com", username, username))
                    .build())
                .name(Name.builder()
                    .givenName("Test")
                    .familyName("User")
                    .build())
                .password(password)
                .userName(username)
                .build())
            .map(CreateUserResponse::getId)
            .delayUntil(userId -> Flux.fromIterable(GROUPS)
                .flatMap(group -> uaaClient.groups()
                    .list(ListGroupsRequest.builder()
                        .filter(String.format("displayName eq \"%s\"", group))
                        .build())
                    .flatMapIterable(ListGroupsResponse::getResources)
                    .singleOrEmpty()
                    .map(Group::getId)
                    .switchIfEmpty(uaaClient.groups()
                        .create(CreateGroupRequest.builder()
                            .displayName(group)
                            .build())
                        .map(CreateGroupResponse::getId))
                    .flatMap(groupId -> uaaClient.groups()
                        .addMember(AddMemberRequest.builder()
                            .groupId(groupId)
                            .memberId(userId)
                            .origin("uaa")
                            .type(MemberType.USER)
                            .build()))))
            .doOnSubscribe(s -> this.logger.debug(">> USER ({}/{}) <<", username, password))
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(id -> this.logger.debug("<< USER ({})>>", id))
            .cache();
    }

    @Bean
    String username(NameFactory nameFactory) {
        return nameFactory.getUserName();
    }

    private static final class FailingDeserializationProblemHandler extends DeserializationProblemHandler {

        @Override
        public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) {
            fail(String.format("Found unexpected property %s in payload for %s", propertyName, beanOrClass.getClass().getName()));
            return false;
        }

    }

}
