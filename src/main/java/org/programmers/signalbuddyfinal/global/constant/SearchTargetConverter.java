package org.programmers.signalbuddyfinal.global.constant;

import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SearchTargetConverter implements Converter<String, SearchTarget> {

    @Override
    public SearchTarget convert(String source) {
        for (SearchTarget target : SearchTarget.values()) {
            if (target.getValue().equals(source)) {
                return target;
            }
        }
        throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
    }
}
