create table if not exists github_events(
  id bigint primary key,
  type varchar(50) not null,
  created_at timestamp not null,
  repo_name varchar(100) not null,
  repo_url varchar(150) not null,
  author varchar(100) not null,
  org varchar(100)
);