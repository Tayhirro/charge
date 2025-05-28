
package com.bupt.charger.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * @author wyf （ created: 2023-06-03 16:41 )
 */
@Data
public class GetTimeNowResponse extends Resp {
    @JsonProperty("timestamp")
    LocalDateTime timestamp;
}