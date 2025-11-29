package com.example.AR_BE.domain.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileURLDTOResponse {
    private List<String> fileUrls;
}
