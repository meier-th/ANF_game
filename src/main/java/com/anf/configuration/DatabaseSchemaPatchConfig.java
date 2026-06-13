package com.anf.configuration;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaPatchConfig {

  private final DataSource dataSource;

  @PostConstruct
  public void ensureSpellPointsColumnExists() {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute(
          "ALTER TABLE statistics "
              + "ADD COLUMN IF NOT EXISTS spell_points INT NOT NULL DEFAULT 0");
      statement.execute(
          "INSERT INTO spells (name, base_damage, damage_per_level, base_chakra_consumption, chakra_consumption_per_level, req_level) "
              + "VALUES "
              + "('Earth Strike', 12, 3, 7, 3, 1), "
              + "('Water Strike', 20, 4, 10, 4, 5), "
              + "('Fire Strike', 40, 5, 15, 5, 12), "
              + "('Air Strike', 70, 10, 20, 10, 25) "
              + "ON CONFLICT (name) DO NOTHING");
    } catch (SQLException exception) {
      log.warn("Unable to apply DB patch for statistics.spell_points: {}", exception.getMessage());
    }
  }
}

