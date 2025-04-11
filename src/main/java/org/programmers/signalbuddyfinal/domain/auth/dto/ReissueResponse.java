package org.programmers.signalbuddyfinal.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

@Getter
@AllArgsConstructor
public class ReissueResponse {

    private HttpHeaders httpHeaders;
}
