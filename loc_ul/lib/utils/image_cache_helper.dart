import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

class ImageCacheHelper {
  static Future<String?> downloadAndSaveImage(String url) async {
    try {
      final dir = await getApplicationDocumentsDirectory();
      final filename = p.basename(url);
      final file = File('${dir.path}/$filename');

      // Sinon, on essaie de la télécharger
      final response = await http.get(Uri.parse(url));
      if (response.statusCode == 200) {
        await file.writeAsBytes(response.bodyBytes);
        return file.path;
      } else {
        // Si l'image existe déjà en local, on la retourne directement
        if (await file.exists()) {
          return file.path;
        } else {
          // Téléchargement échoué mais fichier non existant : retourner null
          return null;
        }
      }
    } catch (e) {
      return null;
    }
  }
}
