# App-specific R8 rules.
#
# Keep this file narrow. Room, Hilt, Compose, and kotlinx.serialization expose
# their own consumer rules. Add a keep rule only when a minified release has a
# verified runtime issue that reflection/code generation cannot otherwise solve.
