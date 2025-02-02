module braidtoolkit.api {
  requires static lombok;
  requires jsr305;

  exports braid.society.secret.braidtoolkit.versions;
  exports braid.society.secret.braidtoolkit.concurrent;
  exports braid.society.secret.braidtoolkit.api;
}