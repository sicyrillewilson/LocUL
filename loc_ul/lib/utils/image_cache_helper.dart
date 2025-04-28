import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'dart:convert'; // Pour base64

class ImageCacheHelper {
  static Future<String?> downloadAndSaveImage(String url) async {
    try {
      final dir = await getApplicationDocumentsDirectory();
      final filename = p.basename(url);

      // Cas 1 : Si c'est une image encodée en base64
      if (url.startsWith('data:image')) {
        final base64String = url.split(',').last;
        final bytes = base64Decode(base64String);

        final file = File('${dir.path}/$filename.jpg'); // on force .jpg
        await file.writeAsBytes(bytes);
        return file.path;
      }

      // Cas 2 : Sinon, téléchargement normal
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
