typedef struct {
    int object;
    float score;
    float left_x;
    float top_y;
    float width;
    float height;
} ImageObject;

typedef struct {
    ImageObject* image_object_list;
    int size;
    int category;
} DetectionResult;

#define NO_OF_IMAGE_CATEGORIES 10
#define NO_OF_TRAINING_IMAGE 803
#define NO_OF_OBJECTS 80


#define BATHROOM 1
#define BEDROOM 2
#define BOOKSTORE 3
#define CHILDRENROOM 4
#define DININGROOM 5
#define KITCHEN 6
#define LIVINGROOM 7
#define LOBBY 8
#define OFFICE 9
#define RESTAURANT 10


//int current_image;
//int image_fv[NO_OF_OBJECT_COCO];
//int no_of_undetected_image;
//const char *image_categories[] = { "BATHROOM", "BEDROOM", "BOOKSTORE", "CHILDRENROOM", "DININGROOM", "KITCHEN" , "LIVINGROOM" , "LOBBY" , "OFFICE" , "RESTAURANT" };
//const char *image_categories[] = { "bathroom", "bedroom", "bookstore", "childrenroom", "diningroom", "kitchen" , "livingroom" , "lobby" , "office" , "restaurant" };

