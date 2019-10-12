package com.upgrade.islandrsvsrv.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampingUser {

	private String firstName;
	private String lastName;
	private String email;
}
