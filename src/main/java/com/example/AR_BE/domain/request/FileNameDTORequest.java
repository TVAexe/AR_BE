package com.example.AR_BE.domain.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileNameDTORequest {
    private List<String> fileNames;
}
