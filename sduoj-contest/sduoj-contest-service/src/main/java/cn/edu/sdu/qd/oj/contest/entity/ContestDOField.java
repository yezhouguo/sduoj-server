/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.contest.entity;

public class ContestDOField {
    public static final String TABLE_NAME = "oj_contest";
    public static final String ID = "ct_id";
    public static final String GMT_CREATE = "ct_gmt_create";
    public static final String GMT_MODIFIED = "ct_gmt_modified";
    public static final String FEATURES = "ct_features";
    public static final String VERSION = "ct_version";
    public static final String DELETED = "ct_is_deleted";
    public static final String IS_PUBLIC = "ct_is_public";
    public static final String TITLE = "ct_title";
    public static final String USER_ID = "u_id";
    public static final String GMT_START = "ct_gmt_start";
    public static final String GMT_END = "ct_gmt_end";
    public static final String PASSWORD = "ct_password";
    public static final String SOURCE = "ct_source";
    public static final String PARTICIPANT_NUM = "ct_participant_num";
    public static final String MARKDOWN_DESCRIPTION = "ct_markdown_description";
    public static final String PROBLEMS = "ct_problems";
    public static final String PARTICIPANTS = "ct_participants";
    public static final String UNOFFICIAL_PARTICIPANTS = "ct_unofficial_participants";
}