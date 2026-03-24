using UnityEngine;
using UnityEngine.EventSystems;

namespace ProjectRelay.Player
{
    public class JoystickLikeInput : MonoBehaviour, IPointerDownHandler, IDragHandler, IPointerUpHandler
    {
        [SerializeField] private RectTransform knob;
        [SerializeField] private float maxRadius = 60f;

        public Vector2 InputVector { get; private set; }

        public void OnPointerDown(PointerEventData eventData) => OnDrag(eventData);

        public void OnDrag(PointerEventData eventData)
        {
            RectTransform rt = transform as RectTransform;
            if (rt == null) return;

            RectTransformUtility.ScreenPointToLocalPointInRectangle(rt, eventData.position, eventData.pressEventCamera, out var localPoint);
            Vector2 clamped = Vector2.ClampMagnitude(localPoint, maxRadius);
            InputVector = clamped / maxRadius;

            if (knob != null)
                knob.anchoredPosition = clamped;
        }

        public void OnPointerUp(PointerEventData eventData)
        {
            InputVector = Vector2.zero;
            if (knob != null)
                knob.anchoredPosition = Vector2.zero;
        }
    }
}
