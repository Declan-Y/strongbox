package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.controllers.support.BaseUrlEntityBody;
import org.carlspring.strongbox.controllers.support.PortEntityBody;
import org.carlspring.strongbox.forms.configuration.ServerSettingsForm;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.support.ConfigurationException;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import java.io.IOException;

import io.swagger.annotations.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/configuration/strongbox")
@Api(value = "/api/configuration/strongbox")
public class ServerConfigurationController
        extends BaseConfigurationController
{

    static final String SUCCESSFUL_SAVE_SERVER_SETTINGS = "The server settings were updated successfully.";
    static final String FAILED_SAVE_SERVER_SETTINGS = "Server settings cannot be saved because the submitted form contains errors!";

    public ServerConfigurationController(ConfigurationManagementService configurationManagementService)
    {
        super(configurationManagementService);
    }

    @ApiOperation(value = "Updates the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The base URL was updated."),
                            @ApiResponse(code = 400,
                                         message = "Could not update the base URL of the service.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_BASE_URL')")
    @PutMapping(value = "/baseUrl",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setBaseUrl(@ApiParam(value = "The base URL", required = true)
                                     @RequestBody BaseUrlEntityBody baseUrlEntity,
                                     @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            String newBaseUrl = baseUrlEntity.getBaseUrl();
            configurationManagementService.setBaseUrl(newBaseUrl);

            logger.info("Set baseUrl to [{}].", newBaseUrl);

            return ResponseEntity.ok(getResponseEntityBody("The base URL was updated.", accept));
        }
        catch (ConfigurationException e)
        {
            String message = "Could not update the base URL of the service.";
            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Returns the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "",
                                         response = String.class),
                            @ApiResponse(code = 404,
                                         message = "No value for baseUrl has been defined yet.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_BASE_URL')")
    @GetMapping(value = "/baseUrl",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getBaseUrl(@RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws IOException
    {
        if (configurationManagementService.getBaseUrl() != null)
        {
            return ResponseEntity.ok(getBaseUrlEntityBody(configurationManagementService.getBaseUrl(), accept));
        }
        else
        {
            String message = "No value for baseUrl has been defined yet.";
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The port was updated."),
                            @ApiResponse(code = 400,
                                         message = "Could not update the strongbox port.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_PORT')")
    @PutMapping(value = "/port/{port}",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setPort(@ApiParam(value = "The port of the service", required = true)
                                  @PathVariable int port,
                                  @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            configurationManagementService.setPort(port);

            logger.info("Set port to {}. This operation will require a server restart.", port);

            return ResponseEntity.ok(getResponseEntityBody("The port was updated.", accept));
        }
        catch (ConfigurationException e)
        {
            String message = "Could not update the strongbox port.";
            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The port was updated."),
                            @ApiResponse(code = 400,
                                         message = "Could not update the strongbox port.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_PORT')")
    @PutMapping(value = "/port",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setPort(@ApiParam(value = "The port of the service", required = true)
                                  @RequestBody PortEntityBody portEntity,
                                  @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        return setPort(portEntity.getPort(), accept);
    }

    @ApiOperation(value = "Returns the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "",
                                         response = String.class) })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_PORT')")
    @GetMapping(value = "/port",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getPort(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        return ResponseEntity.ok(getPortEntityBody(configurationManagementService.getPort(), accept));
    }

    @ApiOperation(value = "Sets the server settings of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_SAVE_SERVER_SETTINGS),
                            @ApiResponse(code = 400, message = FAILED_SAVE_SERVER_SETTINGS) })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_SET_BASE_URL', 'CONFIGURATION_SET_PORT')")
    @PostMapping(value = "/serverSettings",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setServerSettings(@RequestBody @Validated ServerSettingsForm serverSettingsForm,
                                            BindingResult bindingResult,
                                            @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_SAVE_SERVER_SETTINGS, bindingResult);
        }

        configurationManagementService.setBaseUrl(serverSettingsForm.getBaseUrl());
        configurationManagementService.setPort(serverSettingsForm.getPort());

        return ResponseEntity.ok(getResponseEntityBody(SUCCESSFUL_SAVE_SERVER_SETTINGS, acceptHeader));
    }

    private Object getBaseUrlEntityBody(String baseUrl,
                                        String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new BaseUrlEntityBody(baseUrl);
        }
        else
        {
            return baseUrl;
        }
    }

    private Object getPortEntityBody(int port,
                                     String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new PortEntityBody(port);
        }
        else
        {
            return String.valueOf(port);
        }
    }
}
