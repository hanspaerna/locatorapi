package ee.lagunemine.locatorapi.controller;

import ee.lagunemine.locatorapi.dto.StationBaseRequestDTO;
import ee.lagunemine.locatorapi.dto.StationMobilePositionDTO;
import ee.lagunemine.locatorapi.service.StationService;
import ee.lagunemine.locatorapi.validator.StationMobileExists;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@Validated
@RequestMapping("/stations")
class StationController {
    private StationService stationService;
    private ModelMapper mapper;
    private Logger logger;

    private final static String ERROR_INTERNAL = "An internal error has occurred";
    private final static String ERROR_LOG_PREFIX = "An exception in controller has been occurred";

    public StationController(StationService stationService, ModelMapper mapper, Logger logger) {
        this.stationService = stationService;
        this.mapper = mapper;
        this.logger = logger;
    }

    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    public Map<String, String> handleDefaultErrors(HttpServletRequest request, Exception e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseStatus annotation = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);

        // override HTTP status codes for some of built-in exceptions
        if (e.getClass().equals(MissingServletRequestParameterException.class)) {
            httpStatus = HttpStatus.BAD_REQUEST;
        } else if (e.getClass().equals(ConstraintViolationException.class)) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (annotation != null) {
            httpStatus = annotation.value();
        }

        Map<String, String> error = new HashMap<>();

        error.put("code", httpStatus.toString());
        error.put("error", httpStatus != HttpStatus.INTERNAL_SERVER_ERROR ? e.getMessage() : ERROR_INTERNAL);
        error.put("path", request.getRequestURL().toString());
        error.put("time", new Date().toString());

        logger.error(ERROR_LOG_PREFIX, e);

        return error;
    }

    @PostMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String update(@Valid @RequestBody StationBaseRequestDTO requestDto) {
        return "TODO";
    }

    @GetMapping("/mobile/find")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public StationMobilePositionDTO getMobileStationPosition(@StationMobileExists Integer stationId) {
        StationMobilePositionDTO responseDto = new StationMobilePositionDTO();
        mapper.map(stationService.getMobileStation(stationId), responseDto);

        return responseDto;
    }

    @PostMapping("/base/new")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public HashMap<String, Integer> createBaseStation() {
        return new HashMap<String, Integer>() {{
            put("newBaseStationId", stationService.createBaseStation());
        }};
    }
}
