package net.skykingdoms.backend.base.data.tags;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class Tag {
    int id;
    String name;
    SeasonObtained season;
    YearObtained year;
}
