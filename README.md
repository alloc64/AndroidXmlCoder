# AndroidXmlCoder

Almost complete reader/writer for binary Android XML files supporting tag manipulation and manifest merging.

See class ManifestMergerTestMain for usage.

This coder was tested on few thousands obfuscated and non-obfuscated APKs, and few bugs were be found, mostly on obfuscated APKs in validation phase. 
That's why, most of the sanity checks are commented out, however modified manifest shall be working and installable by AAPT2. 

Based on https://github.com/ntop001/AXMLEditor 
Kudos to author of this repository.  

## License

Licensed under GPLv2 as original repository project.
See [LICENSE](LICENSE).
