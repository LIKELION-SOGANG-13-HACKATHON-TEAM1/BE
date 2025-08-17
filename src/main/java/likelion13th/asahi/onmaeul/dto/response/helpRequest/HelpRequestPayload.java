package likelion13th.asahi.onmaeul.DTO.response.helpRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpRequestPayload {
    List<HelpRequestItem> helpRequestItems;
}
